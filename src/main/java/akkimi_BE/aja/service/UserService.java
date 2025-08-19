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
                .email(emailRequestDto.getEmail())
                .passwordHash(passwordEncoder.encode(emailRequestDto.getPassword()))
                .socialId("LOCAL_EMAIL:" + emailRequestDto.getEmail()) //토큰 발급을 위해
                .socialType(SocialType.LOCAL_EMAIL) // 로컬 가입표시
                .role(Role.USER)
                .build();

        return userRepository.save(user).getUserId();
    }

    @Transactional
    public Long signupWithPhone(PhoneRequestDto phoneRequestDto) {
        // 존재하는 회원인지 한 번 더 검사
        if (userRepository.existsByPhoneNumber(phoneRequestDto.getPhoneNumber())) {
            throw new CustomException(HttpErrorCode.VALIDATE_EXISTED_PHONE);
        }

        User user = User.builder()
                .phoneNumber(phoneRequestDto.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(phoneRequestDto.getPassword()))
                .socialId("LOCAL_PHONE:" + phoneRequestDto.getPhoneNumber()) //토큰 발급을 위해
                .socialType(SocialType.LOCAL_PHONE) // 로컬 가입표시
                .role(Role.USER)
                .build();

        return userRepository.save(user).getUserId();
    }

    @Transactional
    public TokenResponse loginWithEmail(EmailRequestDto emailLoginRequestDto) {

        User user = userRepository.findByEmail(emailLoginRequestDto.getEmail())
                .orElseThrow(() -> new CustomException(HttpErrorCode.LOGIN_BAD_EMAIL));

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(emailLoginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new CustomException(HttpErrorCode.LOGIN_BAD_PASSWORD);
        }

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse loginWithPhone(PhoneRequestDto phoneLoginRequestDto) {
        User user = userRepository.findByPhoneNumber(phoneLoginRequestDto.getPhoneNumber())
                .orElseThrow(() -> new CustomException(HttpErrorCode.LOGIN_BAD_PHONE));

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(phoneLoginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new CustomException(HttpErrorCode.LOGIN_BAD_PASSWORD);
        }

        return issueTokens(user);
    }

    public Boolean validatePhone(PhoneValidateRequestDto phoneValidateRequestDto) {
        return !userRepository.existsByPhoneNumber(phoneValidateRequestDto.getPhoneNumber());
    }

    public Boolean validateEmail(EmailValidateRequestDto emailValidateRequestDto) {
        return !userRepository.existsByEmail(emailValidateRequestDto.getEmail());
    }


    @Transactional
    protected TokenResponse issueTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getSocialId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getSocialId());
        log.info("JWT 토큰 생성 완료");

        refreshTokenService.store(user, refreshToken, jwtUtil.getRefreshExpirySeconds());

        return TokenResponse.builder()
                .success(true)
                .message("로그인 성공")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .user(TokenResponse.UserInfo.builder()
                        .userId(user.getUserId())
                        .socialId(user.getSocialId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole().name())
                        .socialType(user.getSocialType().name())
                        .build())
                .build();
    }
}
