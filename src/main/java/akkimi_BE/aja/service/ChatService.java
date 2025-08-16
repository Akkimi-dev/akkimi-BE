package akkimi_BE.aja.service;

import akkimi_BE.aja.entity.ChatMessage;
import akkimi_BE.aja.entity.Speaker;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.ChatMessageRepository;
import io.jsonwebtoken.lang.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;
    private final MaltuService maltuService;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public String talk(User user, String userMessage) {

        Long maltuId = user.getCurrentMaltuId();

        // 말투 시스템 프롬프트 조회
        String systemPrompt = maltuService.resolveTonePrompt(user);

        //유저 메시지 저장
        chatMessageRepository.save(ChatMessage.of(user, maltuId, Speaker.USER, userMessage, false));

        String assistantReply;
        // 3) 모델 호출 (자유 대화이므로 0.7 권장)
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
        chatMessageRepository.save(ChatMessage.of(user, maltuId, Speaker.BOT, assistantReply, false));

        // 5) 최종 응답 반환
        return assistantReply;
    }
}
