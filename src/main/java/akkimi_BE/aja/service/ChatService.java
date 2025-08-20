package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.ChatRequestDto;
import akkimi_BE.aja.dto.response.ChatHistoryResponseDto;
import akkimi_BE.aja.entity.ChatMessage;
import akkimi_BE.aja.entity.Speaker;
import akkimi_BE.aja.entity.TodayConsumption;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.global.exception.CustomException;
import akkimi_BE.aja.global.exception.HttpErrorCode;
import akkimi_BE.aja.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatClient chatClient;
    private final MaltuService maltuService;
    private final ChatMessageRepository chatMessageRepository;
    private final FeedbackPromptBuilder feedbackPromptBuilder;

    // 최근 대화 조회(limit 없으면 30개 기본값)
    public ChatHistoryResponseDto getMessages(User user, Integer limit, Long beforeId) {
        int size = Math.max(1, Math.min(limit == null ? 30 : limit, 100));

        List<ChatMessage> desc = chatMessageRepository.findSlice(
                user.getUserId(), beforeId, PageRequest.of(0, size + 1));

        boolean hasMore = desc.size() > size;
        if (hasMore) desc = desc.subList(0, size);

        // 날짜 라벨링
        DateTimeFormatter dateHeaderFmt = DateTimeFormatter.ofPattern("yyyy. M월 d일 a h:mm", Locale.KOREA);
        DateTimeFormatter timeHeaderFmt = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA);

        List<ChatHistoryResponseDto.MessageDto> items = new ArrayList<>(desc.size());

        ChatMessage prev = null;
        for (ChatMessage cur : desc) {
            LocalDateTime nowTs = cur.getCreatedAt();

            boolean showDate = false;
            boolean showTime = false;

            if (prev != null) {
                // 날짜가 바뀌면 날짜 라벨
                showDate = !prev.getCreatedAt().toLocalDate().equals(nowTs.toLocalDate());

                // 같은 날짜이면서 60분 이상 간격이면 시간 라벨
                if (!showDate) {
                    long gapMin = Duration.between(prev.getCreatedAt(), nowTs).toMinutes();
                    showTime = gapMin >= 60;
                }
            }
            // prev == null (첫 메시지)는 showDate=false, showTime=false 무조건 라벨 x

            items.add(ChatHistoryResponseDto.MessageDto.builder()
                    .chatId(cur.getChatId())
                    .speaker(cur.getSpeaker().name())
                    .message(cur.getMessage())
                    .createdAt(nowTs.toString())
                    // 날짜 바뀌면 날짜+시간 라벨 하나만
                    .showDate(showDate)
                    .dateLabel(showDate ? nowTs.format(dateHeaderFmt) : null)
                    // 60분 이상 간격이면 시간만
                    .showTime(!showDate && showTime)
                    .timeLabel((!showDate && showTime) ? nowTs.format(timeHeaderFmt) : null)
                    .build());

            prev = cur;
        }

        Long nextBeforeId = hasMore ? desc.get(desc.size() - 1).getChatId() : null;

        return ChatHistoryResponseDto.builder()
                .messages(items)
                .hasMore(hasMore)
                .nextBeforeId(nextBeforeId)
                .build();
    }

    // 1) 메시지 저장
    @Transactional
    public Long saveMessage(User user, ChatRequestDto chatRequestDto) {
        Long maltuId = user.getCurrentMaltuId();

        //유저 메시지 저장
        Long messageId = chatMessageRepository.save(
                ChatMessage.of(user, maltuId, Speaker.USER, chatRequestDto.getMessage(), false)
        ).getChatId();

        return messageId;
    }

    // 2) 스트림 대화 답변 받기
    @Transactional
    public SseEmitter streamReply(User user, Long messageId) {
        ChatMessage userMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.MESSAGE_NOT_FOUND));

        if (!userMessage.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(HttpErrorCode.FORBIDDEN_MESSAGE_ACCESS);
        }

        //프롬프트
        String systemPrompt = maltuService.resolveMaltuPrompt(user);

        SseEmitter emitter = new SseEmitter(0L);
        emitter.onTimeout(emitter::complete);
        emitter.onCompletion(() -> log.debug("SSE completed for messageId={}", messageId));

        CompletableFuture.runAsync(() -> {
            StringBuilder sb = new StringBuilder(1024);

            try {
                // 스트림 시작 전에 meta 한 번 전송
                sendEvent(emitter, "meta", "{\"type\":\"start\"}");

                // 1) 스트림 생성 단계에서 발생하는 NonTransientAiException 대비
                Flux<String> stream;
                try {
                    stream = chatClient
                            .prompt()
                            .system(systemPrompt)
                            .user(userMessage.getMessage())
                            .options(ChatOptions.builder().temperature(0.7).build())
                            .stream()
                            .content();
                } catch (NonTransientAiException quotaEx) {
                    handleQuotaFallback(emitter, user, userMessage, sb);
                    return; // 더 진행하지 않고 종료
                }

                // 2) 스트림 진행 중(onError) 발생하는 NonTransientAiException 대비
                stream
                        .timeout(Duration.ofMinutes(2))
                        .doOnNext(token -> {
                            sb.append(token);
                            sendEvent(emitter, "message", token);
                        })
                        .doOnError(ex -> {
                            if (ex instanceof NonTransientAiException) {
                                log.warn("SSE NonTransientAiException: {}", ex.getMessage());
                                quotaFriendlyFinish(emitter, user, userMessage, sb);
                            } else {
                                log.warn("SSE stream error: {}", ex.getMessage(), ex);
                                sendEvent(emitter, "error", "{\"message\":\"stream_failed\"}");
                                emitter.completeWithError(ex);
                            }
                        })
                        .doOnComplete(() -> {
                            try {
                                ChatMessage savedBot = chatMessageRepository.save(
                                        ChatMessage.of(user, userMessage.getMaltuId(), Speaker.BOT, sb.toString(), false)
                                );
                                sendEvent(emitter, "done", "{\"finalMessageId\":" + savedBot.getChatId() + "}");
                                emitter.complete();
                            } catch (DataAccessException e) { //저장 실패 -> 프론트 무한 대기 x
                                log.error("Persist failed: {}", e.getMessage(), e);
                                sendEvent(emitter, "error", "{\"message\":\"persist_failed\"}");
                                emitter.completeWithError(e);
                            }
                        })
                        .subscribe();

            } catch (Exception e) {
                log.error("SSE streaming failed: {}", e.getMessage(), e);
                try {
                    sendEvent(emitter, "error", "{\"message\":\"internal_error\"}");
                } finally {
                    emitter.completeWithError(e);
                }
            }
        });

        return emitter;
    }




    /** 요금/쿼터 초과 등 비재시도 오류일 때 사용자 친절 메시지로 종료(스트림 생성 단계에서 발생한 경우). */
    private void handleQuotaFallback(SseEmitter emitter, User user, ChatMessage userMessage, StringBuilder sb) {
        String fallback = "현재 AI 할당량이 초과되어 임시로 답변을 생성할 수 없어요. 잠시 후 다시 시도해 주세요 🙏";
        sb.append(fallback);
        sendEvent(emitter, "message", fallback);

        ChatMessage savedBot = chatMessageRepository.save(
                ChatMessage.of(user, userMessage.getMaltuId(), Speaker.BOT, sb.toString(), false)
        );
        sendEvent(emitter, "done", "{\"finalMessageId\":" + savedBot.getChatId() + "}");
        emitter.complete();
    }

    /** 스트림 진행 중 doOnError에서 NonTransientAiException을 만났을 때 동일한 마무리 처리. */
    private void quotaFriendlyFinish(SseEmitter emitter, User user, ChatMessage userMessage, StringBuilder sb) {
        String fallback = "현재 AI 할당량이 초과되어 임시로 답변을 생성할 수 없어요. 잠시 후 다시 시도해 주세요 🙏";
        sb.append(fallback);
        try {
            sendEvent(emitter, "message", fallback);
            ChatMessage savedBot = chatMessageRepository.save(
                    ChatMessage.of(user, userMessage.getMaltuId(), Speaker.BOT, sb.toString(), false)
            );
            sendEvent(emitter, "done", "{\"finalMessageId\":" + savedBot.getChatId() + "}");
            emitter.complete();
        } catch (Exception persistEx) {
            log.error("Failed to persist fallback message: {}", persistEx.getMessage(), persistEx);
            sendEvent(emitter, "error", "{\"message\":\"internal_error\"}");
            emitter.completeWithError(persistEx);
        }
    }

    private void sendEvent(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    public void sendConsumptionFeedBack(User user, TodayConsumption consumption) {
        Long maltuId = user.getCurrentMaltuId();

        String systemPrompt = maltuService.resolveMaltuPrompt(user);
        String userMessage = feedbackPromptBuilder.buildUserUtterance(consumption);
        ChatMessage saveUser = chatMessageRepository.save(
                ChatMessage.of(user, maltuId, Speaker.USER, userMessage, true)
        );

        String assistantReply;
        try {
            assistantReply = chatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .options(ChatOptions.builder().temperature(0.4).build())
                    .call()
                    .content();
        }catch (org.springframework.ai.retry.NonTransientAiException e){
            assistantReply = "지금은 피드백을 생성하기 어려워요. 잠시 후 다시 시도해주세요";
        }

        chatMessageRepository.save(
                ChatMessage.of(user, maltuId, Speaker.BOT, assistantReply, true)
        );
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

