package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.response.ChatHistoryResponseDto;
import akkimi_BE.aja.dto.response.ChatResponseDto;
import akkimi_BE.aja.entity.ChatMessage;
import akkimi_BE.aja.entity.Speaker;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatClient chatClient;
    private final MaltuService maltuService;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatResponseDto talk(User user, String userMessage) {

        Long maltuId = user.getCurrentMaltuId();

        // 말투 시스템 프롬프트 조회
        String systemPrompt = maltuService.resolveTonePrompt(user);

        //유저 메시지 저장
        ChatMessage savedUser = chatMessageRepository.save(ChatMessage.of(user, maltuId, Speaker.USER, userMessage, false));

        String assistantReply;
        // 모델 호출 (자유 대화이므로 0.7 권장)
        try {
            assistantReply = chatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .options(ChatOptions.builder().temperature(0.7).build())
                    .call()
                    .content();
        } catch (org.springframework.ai.retry.NonTransientAiException e) {
            // 429 등 요금/쿼터 문제
            assistantReply = "현재 AI 할당량이 초과되어 임시로 답변을 생성할 수 없어요. " +
                    "잠시 후 다시 시도해 주세요 🙏";
        }
        // 4) 모델 응답 저장
        ChatMessage savedBot = chatMessageRepository.save(ChatMessage.of(user, maltuId, Speaker.BOT, assistantReply, false));

        // 5) 응답 DTO
        return ChatResponseDto.builder()
                .userMessage(ChatResponseDto.MessageDto.builder()
                        .messageId(savedUser.getChatId())
                        .role(savedUser.getSpeaker().name())
                        .text(savedUser.getMessage())
                        .createdAt(savedUser.getCreatedAt().toString())
                        .build())
                .botMessage(ChatResponseDto.MessageDto.builder()
                        .messageId(savedBot.getChatId())
                        .role(savedBot.getSpeaker().name())
                        .text(savedBot.getMessage())
                        .createdAt(savedBot.getCreatedAt().toString())
                        .build())
                .build();
    }


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
            boolean showDate = (prev == null) ||
                    !prev.getCreatedAt().toLocalDate().equals(nowTs.toLocalDate());

            boolean showTime = false;
            if (!showDate) {
                long gapMin = Duration.between(prev.getCreatedAt(), nowTs).toMinutes();
                showTime = gapMin >= 60; // 60분 이상만 시간 헤더
            }

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

        Long nextBeforeId = hasMore ? desc.get(0).getChatId() : null;

        return ChatHistoryResponseDto.builder()
                .messages(items)
                .hasMore(hasMore)
                .nextBeforeId(nextBeforeId)
                .build();

    }
}

