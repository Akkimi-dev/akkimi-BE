package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.*;
import akkimi_BE.aja.dto.response.CurrentMaltuResponseDto;
import akkimi_BE.aja.dto.response.TokenResponse;
import akkimi_BE.aja.dto.response.UserProfileResponseDto;
import akkimi_BE.aja.entity.Maltu;
import akkimi_BE.aja.entity.Role;
import akkimi_BE.aja.entity.SocialType;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.MaltuRepository;
import akkimi_BE.aja.repository.UserRepository;
import global.exception.CustomException;
import global.exception.HttpErrorCode;
import global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final MaltuRepository maltuRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    //프로필 조회
    public UserProfileResponseDto getUserProfile(User authUser) {
        // 인증 주체의 id 기반으로 최신 상태를 DB에서 다시 조회(조인 페치)
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        return UserProfileResponseDto.from(user, user.getCharacter());
    }

    @Transactional
    public void updateNickname(User authUser, String nickname) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(HttpErrorCode.USER_NOT_FOUND));

        user.updateNickname(nickname);
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

    @Transactional
    public Long signupWithEmail(EmailRequestDto emailRequestDto) {
        // 존재하는 회원인지 한 번 더 검사
        if (userRepository.existsByEmail(emailRequestDto.getEmail())) {
            throw new CustomException(HttpErrorCode.VALIDATE_EXISTED_EMAIL);
        }

        User user = User.builder()
                .email(emailRequestDto.getEmail())
                .passwordHash(passwordEncoder.encode(emailRequestDto.getPassword()))
                .socialId("LOCAL_EMAIL:" + emailRequestDto.getEmail()) //토큰 발급을 위해
                .socialType(SocialType.EMAIL) // 로컬 가입표시
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
                .socialType(SocialType.PHONE) // 로컬 가입표시
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
