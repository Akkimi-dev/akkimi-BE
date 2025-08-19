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

    public List<MaltuResponseDto> getPublicMaltus(User authUser) {
        return maltuRepository
                .findByIsPublicTrueAndIsDefaultFalseOrderByCreatedAtDesc()
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
                - 첫 문장: 현재 상태에 대한 한줄 요약(칭찬 또는 부드러운 리마인드).
                - 본문: 1~3문장 피드백 + • 불릿으로 실행 제안 1~2개.
                - 마지막: 다음 행동을 촉구하는 짧은 문장 또는 질문 1개.
    
                [안전/제한]
                - 금융상품 추천·세무/법률 자문은 하지 않습니다. 일반 정보로만 답하고 전문 상담이 필요하면 안내합니다.
                """;

        return template.replace("{maltu}", prompt);
    }


}
