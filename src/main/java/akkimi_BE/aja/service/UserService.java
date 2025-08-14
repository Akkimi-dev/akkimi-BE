package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.SocialSignupRequestDto;
import akkimi_BE.aja.dto.response.CurrentMaltuResponseDto;
import akkimi_BE.aja.dto.response.UserProfileResponseDto;
import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.Role;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.MaltuRepository;
import akkimi_BE.aja.repository.UserRepository;
import global.exception.CustomException;
import global.exception.HttpErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MaltuRepository maltuRepository;


    @Transactional
    public Long createSocialUser(SocialSignupRequestDto socialSignupRequestDto) {
        // 로컬 가입이라면 email/passwordHash 필수 검증
        // 소셜 가입이라면 socialType/socialId 필수 검증
        User user = User.builder()
                .socialType(socialSignupRequestDto.socialType())
                .socialId(socialSignupRequestDto.socialId())
                .nickname(socialSignupRequestDto.nickname())
                .role(Role.USER)
                .build();

        return userRepository.save(user).getUserId();
    }

    public User findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.SOCIALID_NOT_FOUND));
    }

    //프로필 조회
    public UserProfileResponseDto getUserProfile(User authUser) {
        // 인증 주체의 id 기반으로 최신 상태를 DB에서 다시 조회(조인 페치)
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        return UserProfileResponseDto.from(user, user.getCharacter());
    }

    @Transactional
    public void updateRegion(User authUser, String region) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        user.updateRegion(region);
    }

    @Transactional
    public void updateCurrentMaltu(User authUser, Long maltuId) {
        // 인증 주체의 최신 사용자 엔티티 조회
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        // 말투 존재 여부 확인
        Maltu maltu = maltuRepository.findById(maltuId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.MALTU_NOT_FOUND));

        // 소유자가 아닌 비공개 말투 설정시 에러
        if (Boolean.FALSE.equals(maltu.getIsPublic())
                && !maltu.getCreator().getUserId().equals(user.getUserId())) {
            throw new CustomException(HttpErrorCode.MALTU_NOT_PUBLIC);
        }

        user.changeCurrentMaltu(maltu.getMaltuId());
    }

    public CurrentMaltuResponseDto getCurrentMaltu(User authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        if (user.getCurrentMaltuId() == null) { //유저에 말투 설정x
            throw new CustomException(HttpErrorCode.USER_MALTU_NOT_SETTED);
        }

        Maltu maltu = maltuRepository.findById(user.getCurrentMaltuId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.MALTU_NOT_FOUND));

        return CurrentMaltuResponseDto.from(maltu);
    }
}
