package akkimi_BE.aja.service;

import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.MaltuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaltuService {
    private final MaltuRepository maltuRepository;

    public String resolveTonePrompt(User user) {
        Long maltuId = user.getCurrentMaltuId(); //null이면 에러처리
        String prompt = "";

        if (maltuId != null) {
            prompt = maltuRepository.findById(maltuId)
                    .filter(m -> m.getCreator().getUserId().equals(user.getUserId()) || Boolean.TRUE.equals(m.getIsPublic()))
                    .map(Maltu::getPrompt)
                    .orElse(null);
        }

        // 기본 말투(없을 때)
        if (prompt == null || prompt.isBlank()) {
            prompt = """
                    너는 친절하고 간결한 재무 코치야.
                    - 반말 대신 부드러운 존댓말을 사용해.
                    - 불필요한 수사는 피하고, 핵심만 명확히.
                    - 금액/퍼센트는 보기 좋게 포맷해.
                    """;
        }

        // 공통 가드레일(말투 + 역할 고정)
        return """
                [역할]
                - 당신은 사용자 소비 기록을 기억하고, 그에 대한 피드백을 주는 대화형 코치입니다.

                [말투 규칙]
                %s

                [출력 스타일]
                - 3~6문장 내로 요점을 말하고, 필요 시 불릿을 사용합니다.
                - 모호하면 구체적 질문 1개만 되묻습니다.
                """.formatted(prompt);
    }
}
