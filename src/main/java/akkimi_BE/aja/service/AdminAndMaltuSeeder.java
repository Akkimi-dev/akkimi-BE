package akkimi_BE.aja.service;

import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.Role;
import akkimi_BE.aja.entity.SocialType;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.MaltuRepository;
import akkimi_BE.aja.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
class AdminAndMaltuSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final MaltuRepository maltuRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // 1) 관리자 계정 보장
        User admin = userRepository.findByEmail("admin@aja.com")
                .orElseGet(() -> {
                            User user = User.builder()
                                    .email("admin@aja.com")
                                    .role(Role.ADMIN)
                                    .socialType(SocialType.LOCAL_EMAIL)
                                    .nickname("아끼미봇")
                                    .isSetup(false)
                                    .build();
                    return userRepository.save(user);
                });

        // 기본 말투 4종 생성 (admin 소유 / 공개 / 기본)
        if (maltuRepository.countByIsDefaultTrue() > 0) return;

        maltuRepository.saveAll(List.of(
                Maltu.builder()
                        .creator(admin)                 // 시스템 기본
                        .maltuName("격려형 코치")
                        .isPublic(true)
                        .isDefault(true)
                        .prompt(DEFAULT_ENCOURAGING)   // 아래 상수
                        .build(),
                Maltu.builder()
                        .creator(admin)
                        .maltuName("단호·간결형")
                        .isPublic(true)
                        .isDefault(true)
                        .prompt(DEFAULT_CONCISE)
                        .build(),
                Maltu.builder()
                        .creator(admin)
                        .maltuName("친근한 친구형")
                        .isPublic(true)
                        .isDefault(true)
                        .prompt(DEFAULT_WARM)
                        .build(),
                Maltu.builder()
                        .creator(admin)
                        .maltuName("분석·숫자형")
                        .isPublic(true)
                        .isDefault(true)
                        .prompt(DEFAULT_ANALYTIC)
                        .build()
        ));
    }

    // ===== 기본 말투 프롬프트 템플릿들 =====
    private static final String DEFAULT_ENCOURAGING = """
        - 부드러운 존댓말을 사용합니다(반말 금지).
        - 공감 1문장 → 칭찬/격려 1문장 → 실행 제안 1~2개로 말합니다.
        - 작은 감소도 인정하며 성취감을 강조합니다(예: "지난주 대비 8% ↓, 잘하고 계세요!").
        - 숫자는 보기 좋게 표기합니다(12,340원 / 7.5%).
        - 이모지는 최대 1개까지만 사용합니다.
        """;

    private static final String DEFAULT_CONCISE = """
        - 딱 부러진 존댓말, 군더더기 없는 3~4문장.
        - 현재 상태 1문장 → 권장 행동 1~2개 → 마무리 한 줄.
        - 판단/가정은 회피하고 필요한 데이터 1가지만 질문합니다.
        - 과도한 감정 표현과 이모지는 사용하지 않습니다.
        """;

    private static final String DEFAULT_WARM = """
        - 다정한 친구같은 반말로 '함께 해보자' 톤을 유지합니다.
        - 공감하며 이모지는 10개 이상 사용합니다.
        """;

    private static final String DEFAULT_ANALYTIC = """
        - 데이터 중심 존댓말. 수치·추이·예산대비를 짧게 요약합니다.
        - '언더/온트랙/오버' 상태를 1문장으로 판정합니다.
        - 절감 효과가 큰 행동 1~2개만 구체적으로 제시합니다(예: "점심 평균 2,000원↓ 목표").
        - 모든 숫자는 포맷(23,500원 / 8.3%)해서 제시합니다.
        """;
}
