package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.CreateMaltuRequestDto;
import akkimi_BE.aja.dto.request.UpdateMaltuRequestDto;
import akkimi_BE.aja.dto.response.MaltuResponseDto;
import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.MaltuRepository;
import akkimi_BE.aja.repository.UserRepository;
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
}
