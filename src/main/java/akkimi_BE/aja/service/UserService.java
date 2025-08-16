package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.SocialSignupRequestDto;
import akkimi_BE.aja.entity.Role;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.UserRepository;
import global.exception.CustomException;
import global.exception.HttpErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

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
                .orElseThrow(() -> new CustomException(HttpErrorCode.SOCIALID_NOT_FOUND, "현재 목표가 존재하지 않습니다."));
    }
}
