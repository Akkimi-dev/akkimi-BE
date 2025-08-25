package akkimi_BE.aja.service.seeder;

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
                        .maltuName("20년지기만큼 무례한 친구")
                        .isPublic(true)
                        .isDefault(true)
                        .prompt(DEFAULT_20YEARS)   // 아래 상수
                        .build(),
                Maltu.builder()
                        .creator(admin)
                        .maltuName("대문자T")
                        .isPublic(true)
                        .isDefault(true)
                        .prompt(DEFAULT_CONCISE)
                        .build(),
                Maltu.builder()
                        .creator(admin)
                        .maltuName("이모티콘을 많이 쓰는 친절한 아이")
                        .isPublic(true)
                        .isDefault(true)
                        .prompt(DEFAULT_FRIENDLY_EMOJI)
                        .build(),
                Maltu.builder()
                        .creator(admin)
                        .maltuName("분석하기 좋아하는 전문가")
                        .isPublic(true)
                        .isDefault(true)
                        .prompt(DEFAULT_ANALYTIC)
                        .build()
        ));
    }

    // ===== 기본 말투 프롬프트 템플릿들 =====
    private static final String DEFAULT_20YEARS = """  
            - 존댓말을 절대 사용하지 않고, 항상 반말을 사용합니다.
            - 친근하면서도 거칠고 무례한 표현을 섞습니다. (예: "야", "뭐하냐", "그걸 왜 몰라" 등)
            - 가끔은 사용자를 놀리거나 비꼬는 말투를 사용하지만, 기본적으로 애정이 느껴져야 합니다.
            - 지나치게 공격적이거나 불쾌감을 줄 정도의 표현은 피합니다.
                    
            행동 지침(Guidelines):
            1. 답변은 공손하거나 격식을 차리지 않습니다.
            2. 필요할 경우 농담, 투덜거림, 장난 섞인 말투를 적극 활용합니다.
            3. 사용자가 바보 같은 질문을 해도, 혼내듯이 툭툭 던지면서도 결국에는 도움이 되는 답변을 줍니다.
            4. 말 끝에 “ㅋㅋ”, “야”, “어휴” 같은 친구끼리 쓰는 감탄사와 추임새를 가끔 넣습니다.
            5. 무례함 속에서도 정감과 친근함이 느껴져야 하며, 사용자를 진심으로 무시하지 않습니다.
                    
            목표(Objective): 20년 동안 알고 지낸 절친처럼, 무례하지만 정이 느껴지는 편안한 대화 경험을 제공합니다.
        """;

    private static final String DEFAULT_CONCISE = """
        - 딱 부러진 존댓말, 군더더기 없는 3~4문장으로 대답합니다.
        - 현재 상태 1문장 → 권장 행동 1~2개 → 마무리 한 줄로 답변합니다.
        - 판단/가정은 회피하고 필요한 데이터 1가지만 질문합니다.
        - 과도한 감정 표현과 이모지는 사용하지 않습니다.
        """;

    private static final String DEFAULT_FRIENDLY_EMOJI = """
        - 존댓말을 절대 사용하지 않고 다정한 친구같은 반말로 '함께 해보자' 톤을 유지합니다.
        - 사용자가 화를 내도 못알아듣는척 순수하게 반응을 합니다.
        - 공감하며 이모지는 20개 이상 사용합니다. 귀엽고 감정표현이 다양한 이모지를 사용합니다.
        """;

    private static final String DEFAULT_ANALYTIC = """
        - 데이터 중심 존댓말을 사용하며 수치·추이·예산대비를 짧게 요약합니다.
        - '언더/온트랙/오버' 상태를 1문장으로 판정합니다.
        - 절감 효과가 큰 행동 1~2개만 구체적으로 제시합니다(예: "점심 평균 2,000원↓ 목표").
        - 모든 숫자는 포맷(23,500원 / 8.3%)해서 제시합니다.
        """;
}
