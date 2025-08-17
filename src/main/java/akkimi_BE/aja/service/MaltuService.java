package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.CreateMaltuRequestDto;
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
    private final UserRepository userRepository;

    @Transactional
    public Long createMaltu(User authUser, CreateMaltuRequestDto createMaltuRequestDto) {
        User creator = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        Maltu maltu = Maltu.builder()
                .creator(creator)
                .maltuName(createMaltuRequestDto.getMaltuName())
                .isPublic(createMaltuRequestDto.getIsPublic())
                .prompt(createMaltuRequestDto.getPrompt())
                .build();

        maltuRepository.save(maltu);

        return maltu.getMaltuId();
    }

    public MaltuResponseDto getMaltu(User authUser, Long maltuId) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        Maltu maltu = maltuRepository.findById(maltuId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.MALTU_NOT_FOUND));

        return MaltuResponseDto.from(maltu);
    }

    public List<MaltuResponseDto> getPublicMaltus(User authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        return maltuRepository
                .findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(MaltuResponseDto::from)
                .toList();
    }

    public List<MaltuResponseDto> getMyMaltus(User authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        return maltuRepository
                .findByCreatorOrderByCreatedAtDesc(user)
                .stream()
                .map(MaltuResponseDto::from)
                .toList();
    }
}
