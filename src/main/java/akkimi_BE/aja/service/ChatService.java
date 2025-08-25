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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final MaltuService maltuService;
    private final ChatMessageRepository chatMessageRepository;
    private final FeedbackPromptBuilder feedbackPromptBuilder;

    // 최근 대화 조회(limit 없으면 30개 기본값)
    @Transactional(readOnly = true)
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

    // 메시지 데이터 DTO
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class MessageData {
        private String message;
        private Long maltuId;
    }

    // 사용자 메시지 조회 - 필요한 데이터만 추출하여 반환
    @Transactional(readOnly = true)
    public MessageData getUserMessageData(User user, Long messageId) {
        ChatMessage userMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.MESSAGE_NOT_FOUND));

        if (!userMessage.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(HttpErrorCode.FORBIDDEN_MESSAGE_ACCESS);
        }

        // 필요한 데이터만 추출하여 트랜잭션 종료
        return new MessageData(userMessage.getMessage(), userMessage.getMaltuId());
    }

    // 2) 스트림 대화 답변 받기 - 트랜잭션 제거 (SSE 장시간 연결로 인한 DB 커넥션 고갈 방지)
    public SseEmitter streamReply(User user, Long messageId) {
        // 트랜잭션 내에서 필요한 데이터만 조회 (트랜잭션은 즉시 종료됨)
        MessageData messageData = getUserMessageData(user, messageId);

        //프롬프트 - User 엔티티를 다시 사용하므로 나중에 수정 필요
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
                            .user(messageData.getMessage())
                            .options(ChatOptions.builder().temperature(0.7).build())
                            .stream()
                            .content();
                } catch (NonTransientAiException quotaEx) {
                    handleQuotaFallback(emitter, user, messageData.getMaltuId(), sb);
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
                                // saveBotMessage는 자체 @Transactional을 가지고 있음
                                Long savedBotId = saveBotMessage(user, messageData.getMaltuId(), finalContent);
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
    private void handleQuotaFallback(SseEmitter emitter, User user, Long maltuId, StringBuilder sb) {
        String fallback = "현재 AI 할당량이 초과되어 임시로 답변을 생성할 수 없어요. 잠시 후 다시 시도해 주세요 🙏";
        sb.append(fallback);
        sendEvent(emitter, "message", fallback);

        // saveBotMessage는 자체 @Transactional을 가지고 있음
        Long savedBotId = saveBotMessage(user, maltuId, sb.toString());
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
    @Transactional(propagation = REQUIRES_NEW)
    public Long saveBotMessage(User user, Long maltuId, String content) {
        ChatMessage savedBot = chatMessageRepository.save(
                ChatMessage.of(user, maltuId, Speaker.BOT, content, false, null)
        );
        return savedBot.getChatId();
    }

    // 일일소비에서 해당 소비의 피드백 받아오기(챗봇만)
    @Transactional(readOnly = true)
    public ChatMessage findFeedbackMessage(Long consumptionId) {
        return chatMessageRepository.findByConsumptionId(consumptionId);
    }
    
    // 여러 소비의 피드백을 한 번에 조회
    @Transactional(readOnly = true)
    public List<ChatMessage> findFeedbackMessages(List<Long> consumptionIds) {
        if (consumptionIds == null || consumptionIds.isEmpty()) {
            return List.of();
        }
        return chatMessageRepository.findByConsumptionIds(consumptionIds);
    }
}

