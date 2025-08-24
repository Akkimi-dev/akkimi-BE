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
                ChatMessage.of(user, maltuId, Speaker.USER, chatRequestDto.getMessage(), false, null)
        ).getChatId();

        return messageId;
    }

    // 2) 스트림 대화 답변 받기 - 트랜잭션 제거 (SSE 장시간 연결로 인한 DB 커넥션 고갈 방지)
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
                                String fallback = "현재 AI 할당량이 초과되어 임시로 답변을 생성할 수 없어요. 잠시 후 다시 시도해 주세요 🙏";
                                sb.append(fallback);
                                sendEvent(emitter, "message", fallback);
                            } else {
                                log.warn("SSE stream error: {}", ex.getMessage());
                                sendEvent(emitter, "error", "{\"message\":\"stream_failed\"}");
                            }
                        })
                        .doFinally(signal -> {
                            // 무조건 실행되는 블록 - 정상이든 에러든 무조건 저장
                            String finalContent = sb.toString();
                            if (finalContent.isEmpty()) {
                                finalContent = "응답 생성 실패";
                            }
                            
                            try {
                                Long savedBotId = saveBotMessage(user, userMessage.getMaltuId(), finalContent);
                                sendEvent(emitter, "done", "{\"finalMessageId\":" + savedBotId + "}");
                                log.info("Bot response saved: {} chars", finalContent.length());
                            } catch (Exception e) {
                                log.error("Failed to save bot response", e);
                                sendEvent(emitter, "error", "{\"message\":\"persist_failed\"}");
                            } finally {
                                emitter.complete();
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

        Long savedBotId = saveBotMessage(user, userMessage.getMaltuId(), sb.toString());
        sendEvent(emitter, "done", "{\"finalMessageId\":" + savedBotId + "}");
        emitter.complete();
    }

    private void sendEvent(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            log.debug("Client disconnected: {}", e.getMessage());
        }
    }

    @Transactional
    public String sendConsumptionFeedBack(User user, TodayConsumption consumption) {
        Long maltuId = user.getCurrentMaltuId();

        String systemPrompt = maltuService.resolveMaltuPrompt(user);
        String userMessage = feedbackPromptBuilder.buildUserUtterance(consumption);
        ChatMessage saveUser = chatMessageRepository.save(
                ChatMessage.of(user, maltuId, Speaker.USER, userMessage, true, consumption.getConsumptionId())
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
                ChatMessage.of(user, maltuId, Speaker.BOT, assistantReply, true, consumption.getConsumptionId())
        );
        return assistantReply;
    }

    // 챗봇 메시지 저장
    @Transactional
    public Long saveBotMessage(User user, Long maltuId, String content) {
        ChatMessage savedBot = chatMessageRepository.save(
                ChatMessage.of(user, maltuId, Speaker.BOT, content, false, null)
        );
        return savedBot.getChatId();
    }

    // 일일소비에서 해당 소비의 피드백 받아오기(챗봇만)
    public ChatMessage findFeedbackMessage(Long consumptionId) {
        return chatMessageRepository.findByConsumptionId(consumptionId);
    }
}

