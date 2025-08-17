package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.CreateMaltuRequestDto;
import akkimi_BE.aja.dto.request.UpdateMaltuRequestDto;
import akkimi_BE.aja.dto.response.MaltuResponseDto;
import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.MaltuRepository;
import global.exception.CustomException;
import global.exception.HttpErrorCode;
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
