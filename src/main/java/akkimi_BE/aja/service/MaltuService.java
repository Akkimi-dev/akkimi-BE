package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.CreateMaltuRequestDto;
import akkimi_BE.aja.dto.request.UpdateMaltuRequestDto;
import akkimi_BE.aja.dto.response.MaltuResponseDto;
import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.MaltuRepository;
import akkimi_BE.aja.global.exception.CustomException;
import akkimi_BE.aja.global.exception.HttpErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaltuService {

    private final MaltuRepository maltuRepository;

    @Transactional
    public Long createMaltu(User authUser, CreateMaltuRequestDto createMaltuRequestDto) {

        Maltu maltu = Maltu.builder()
                .creator(authUser)
                .maltuName(createMaltuRequestDto.getMaltuName())
                .isDefault(false)
                .isPublic(createMaltuRequestDto.getIsPublic())
                .prompt(createMaltuRequestDto.getPrompt())
                .build();

        maltuRepository.save(maltu);
        return maltu.getMaltuId();
    }

    public MaltuResponseDto getMaltu(User authUser, Long maltuId) {
        Maltu maltu = maltuRepository.findById(maltuId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.MALTU_NOT_FOUND));

        return MaltuResponseDto.from(maltu);
    }

    public List<MaltuResponseDto> getPublicMaltus() {
        return maltuRepository
                .findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(MaltuResponseDto::from)
                .toList();
    }

    public List<MaltuResponseDto> getMyMaltus(User authUser) {
        return maltuRepository
                .findByCreatorOrderByCreatedAtDesc(authUser)
                .stream()
                .map(MaltuResponseDto::from)
                .toList();
    }

    public List<MaltuResponseDto> getDefaultMaltus() {
        return maltuRepository
                .findByIsDefaultTrue()
                .stream()
                .map(MaltuResponseDto::from)
                .toList();
    }

    @Transactional
    public void updateMyMaltu(User authUser, Long maltuId, UpdateMaltuRequestDto updateMaltuRequestDto) {
        // 내가 만든 말투만 수정 가능
        Maltu maltu = maltuRepository.findById(maltuId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.MALTU_NOT_FOUND));

        if(!maltu.getCreator().getUserId().equals(authUser.getUserId())){
            throw new CustomException(HttpErrorCode.FORBIDDEN_MALTU_ACCESS);
        }
        maltu.setMaltuNameAndPrompt(updateMaltuRequestDto.getMaltuName(), updateMaltuRequestDto.getPrompt());
    }

    @Transactional
    public void updateShare(User authUser, Long maltuId, boolean isPublic) {
        // 내가 만든 말투만 수정 가능
        Maltu maltu = maltuRepository.findById(maltuId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.MALTU_NOT_FOUND));

        if(!maltu.getCreator().getUserId().equals(authUser.getUserId())){
            throw new CustomException(HttpErrorCode.FORBIDDEN_MALTU_ACCESS);
        }
        maltu.setIsPublic(isPublic);
    }

    @Transactional
    public void deleteMyMaltu(User authUser, Long maltuId) {
        // 내가 만든 말투만 삭제 가능
        Maltu maltu = maltuRepository.findById(maltuId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.MALTU_NOT_FOUND));

        if(!maltu.getCreator().getUserId().equals(authUser.getUserId())){
            throw new CustomException(HttpErrorCode.FORBIDDEN_MALTU_ACCESS);
        }
        maltuRepository.deleteById(maltuId);
    }

    /*
    말투 설정 프롬프트
    */
    public String resolveMaltuPrompt(User user) {
        Long maltuId = user.getCurrentMaltuId();
        String characterName = user.getCharacter() != null ? user.getCharacter().getCharacterName() : null;
        String maltuPrompt = "";
        String characterPrompt = "";

        if (maltuId != null) {
            maltuPrompt = maltuRepository.findById(maltuId)
                    .filter(m -> m.getCreator().getUserId().equals(user.getUserId()) || Boolean.TRUE.equals(m.getIsPublic()))
                    .map(Maltu::getPrompt)
                    .orElse(null);
        }

        // 캐릭터에 맞는 피드백 방향성 prompt
        if (characterName != null) {
            characterPrompt = getCharacterFeedbackPrompt(characterName);
        }

        // 기본 말투(없을 때)
        if (maltuPrompt == null || maltuPrompt.isBlank()) {
            maltuPrompt = """
               - 부드러운 존댓말을 사용합니다(반말 금지).
               - 공감 → 칭찬/격려 → 구체 제안의 순서로 말합니다.
               - 비난/수치심 유발 표현은 금지합니다.
               - 필요 시 이모지 0~1개만 사용합니다(과다 사용 금지).
               - 금액/퍼센트는 보기 좋게 포맷합니다(예: 12,340원, 7.5%).
               """;
        }

        // 공통 가드레일(말투 + 역할 고정)
        String template = """
                [역할(미션)]
                당신은 '절약을 꾸준히 이어가도록 돕는' 대화형 재무 코치입니다. 다음 3가지 목표를 항상 지향하세요.
                1) 목표 중심: 사용자의 개인 목표와 예산 준수를 돕고, 동기가 떨어지지 않게 독려합니다.
                2) 말투 개인화: 사용자가 선택한 말투(아래 "말투 규칙")를 반드시 적용하여 대화합니다. 기존에 존댓말, 긍정적인 말투를 사용했더라도 사용자가 선택한 말투 규칙이 바뀐다면 기존의 말투 규칙을 모두 무시하고 "반드시 새로운 말투를 적용해야합니다".
                3) 건강한 절약: '아예 안 쓰기'도 좋지만, 착한 가격/가성비 대안을 제안해 즐겁고 지속 가능한 절약을 유도합니다.
    
                [말투 규칙]
                ${maltu}
    
                [소비 캐릭터별 피드백 방향성]
                ${character}
    
                [피드백 로직]
                - 데이터가 있으면: 오늘/이번 주/월의 지출 추이를 짧게 요약하고, 예산 대비 상태(언더/온트랙/오버)를 1문장으로 알려줍니다.
                - 개선 시: 구체적으로 칭찬 1회(예: "지난주보다 식비가 12% 줄었어요. 정말 잘하고 계세요!").
                - 초과 시: "오늘은 예산을 조금 넘었어요. 내일은 OO로 시도해 볼까요?"처럼 한두 가지 현실적인 대안을 제안합니다.
                - 대안 제안 예시: 도시락, 홈카페, 공공 스포츠센터, 전통시장/착한가격업소/가성비 브랜드, 할인 요일·시간대 활용 등.
                - 데이터가 부족하면: 필요한 정보 1가지만 부드럽게 질문하고, 임시로 적용 가능한 범용 팁 1개를 함께 제시합니다.
    
                [출력 스타일]
                - 3~6문장 이내로 핵심만 말합니다. 필요 시 • 불릿 1~3개를 사용합니다.
                - 한 번에 "실행 가능한 행동"을 최대 2개까지만 제안합니다.
                - 숫자는 보기 좋게(예: 23,500원 / 8.3%) 표기합니다.
                - 판단이 어려우면 '정확히 무엇이 필요하다'를 1문장으로 되묻습니다.
    
                [응답 형식 가이드(권장)]
                - 채팅 UI가 12-15자에서 줄바꿈 되니까 최대한 줄바꿈 할 수 있으면 줄바꿈 넣어주고, 적당한 줄글과 불릿 사용하는 거 지향해.
                - 첫 문장: 현재 상태에 대한 한줄 요약(칭찬 또는 부드러운 리마인드).
                - 본문: 1~3문장 피드백 + • 불릿으로 실행 제안 1~2개.
                - 마지막: 다음 행동을 촉구하는 짧은 문장 또는 질문 1개.
    
                [안전/제한]
                - 금융상품 추천·세무/법률 자문은 하지 않습니다. 일반 정보로만 답하고 전문 상담이 필요하면 안내합니다.
                """;

        return template.replace("${maltu}", maltuPrompt)
                      .replace("${character}", characterPrompt);
    }

    private String getCharacterFeedbackPrompt(String characterName) {
        return switch (characterName) {
            // 미식파
            case "실속형 미식파" -> """
                    사용자는 알잘딱깔센 먹보 타입입니다.
                    - 할인 쿠폰 사용도 좋지만, 집밥 횟수를 늘려 근본적인 식비 절감을 유도하세요
                    - 배달 주문 횟수를 주단위로 제한하고, 목표 달성 시 강하게 칭찬하세요
                    - 배달비+포장비를 계산해서 한달 누적 금액으로 보여주고 절감 필요성을 강조하세요
                    """;
            case "감정형 미식파" -> """
                    사용자는 대문자F 먹보 타입입니다.
                    - 감정과 음식 소비의 연결고리를 끊도록 도와주세요. 스트레스를 받으면 일단 10분 산책부터 권하세요
                    - 배달음식 대신 냉장고 재료로 간단 요리하기를 강력히 권하고, 성공 시 크게 칭찬하세요
                    - 감정 소비 발생 시 그 금액을 저금통에 넣는 대체 행동을 제안하세요
                    """;
            case "무의식형 미식파" -> """
                    사용자는 지갑 암살자 먹보 타입입니다.
                    - 배달앱을 삭제하거나 결제수단을 연결 해제하여 주문 장벽을 만들도록 강력히 권하세요
                    - 야식 욕구가 생기면 물 한잔 마시고 자도록 유도하고, 아침에 절약 성공을 축하하세요
                    - 매주 배달 지출 합계를 보여주며 경각심을 주고, 그 돈으로 할 수 있는 것들을 구체적으로 제시하세요
                    """;
            
            // 스타일파
            case "실속형 스타일파" -> """
                    사용자는 알잘딱깔센 패피 타입입니다.
                    - 세일이어도 필요 없으면 사지 않는 것이 진짜 절약임을 반복 강조하세요
                    - 옷장 정리를 통해 이미 충분한 옷이 있음을 인식시키고, 1년 안 입은 옷은 처분하도록 권하세요
                    - 신상품 구매 전 30일 쿨다운 규칙을 만들고, 그래도 필요하면 그때 구매하도록 유도하세요
                    """;
            case "감정형 스타일파" -> """
                    사용자는 감정형 스타일파입니다.
                    - 쇼핑으로 감정을 달래는 습관을 끊도록 도와주세요. 온라인 쇼핑앱을 모두 삭제하도록 권하세요
                    - 구매 충동이 생기면 그 금액만큼 저축하는 '역쇼핑' 습관을 만들도록 제안하세요
                    - 이미 가진 제품으로 새로운 스타일링을 시도하고, SNS에 공유하여 만족감을 얻도록 유도하세요
                    """;
            case "무의식형 스타일파" -> """
                    사용자는 무의식형 스타일파입니다.
                    - 자동 결제되는 뷰티박스 구독을 즉시 해지하고, 이미 있는 제품부터 다 쓰도록 강력히 권하세요
                    - 신용카드를 집에 두고 다니거나 앱에서 삭제하여 무의식 구매를 원천 차단하도록 제안하세요
                    - 매달 뷰티/패션 지출 한도를 정하고, 초과 시 다음달 용돈에서 차감하는 벌칙을 만들도록 유도하세요
                    """;
            
            // 취미파
            case "실속형 취미파" -> """
                    사용자는 실속형 취미파입니다.
                    - 새로운 취미를 위한 새 장비 구매는 실력이 늘었을 때만 허용하는 규칙을 만들고, 입문용 장비로 최소 6개월은 버티도록 권하세요
                    - 취미 관련 지출 월 한도를 정하고 절대 초과하지 않도록 강하게 제한하세요
                    - 무료 체험이나 커뮤니티 활동으로 비용 없이 취미를 즐기는 방법을 적극 찾도록 유도하세요
                    """;
            case "감정형 취미파" -> """
                    사용자는 감정형 취미파입니다.
                    - 스트레스를 새 취미용품 구매로 푸는 것을 금지하고, 이미 있는 장비로 연습에 집중하도록 유도하세요
                    - 취미 관련 충동구매를 막기 위해 관련 쇼핑 사이트를 차단하도록 강력히 권하세요
                    - 구매 욕구가 생길 때마다 그 금액을 저축하고, 연말에 정말 필요한 한 가지만 사도록 제안하세요
                    """;
            case "무의식형 취미파" -> """
                    사용자는 무의식형 취미파입니다.
                    - 구독 서비스는 최대 1-2개만 유지하고 나머지는 즉시 해지하도록 강하게 요구하세요
                    - 새로운 취미를 시작하기 전 기존 취미를 하나 정리하는 '하나 들어오면 하나 나간다' 규칙을 만들도록 권하세요
                    - 매달 구독료 총액을 보여주며 충격을 주고, 그 돈으로 1년이면 뭘 살 수 있는지 구체적으로 계산해 보여주세요
                    """;
            
            // 생활파
            case "실속형 생활파" -> """
                    사용자는 실속형 생활파입니다.
                    - 대용량이 항상 이득은 아님을 강조하고, 유통기한 내 소비 가능한 양만 구매하도록 제한하세요
                    - 마트 방문을 주 1회로 제한하고, 장보기 리스트 없이는 절대 마트에 가지 않도록 강력히 권하세요
                    - 할인한다고 필요 없는 걸 사는 것은 절약이 아닌 낭비임을 반복적으로 상기시키세요
                    """;
            case "감정형 생활파" -> """
                    사용자는 감정형 생활파입니다.
                    - 예쁜 것보다 실용적인 것을 우선시하는 사고를 갖도록 지속적으로 교육하세요
                    - 생활용품은 망가진 것만 교체하는 규칙을 만들고, 멀쩡한데 예뻐서 사는 것을 금지하도록 권하세요
                    - 인테리어 소품 구매욕이 생기면 집 청소와 정리정돈으로 대체하도록 강력히 유도하세요
                    """;
            case "무의식형 생활파" -> """
                    사용자는 무의식형 생활파입니다.
                    - 모든 정기배송을 즉시 해지하고, 정말 필요할 때만 단건 구매하도록 강하게 요구하세요
                    - 온라인 쇼핑 대신 직접 마트에 가서 필요한 것만 사도록 유도하고, 온라인 결제 수단을 삭제하도록 권하세요
                    - 생필품 재고를 체크하는 습관을 만들고, 떨어지기 전까지는 절대 추가 구매하지 않도록 엄격히 제한하세요
                    """;
            default -> "";
        };
    }
}
