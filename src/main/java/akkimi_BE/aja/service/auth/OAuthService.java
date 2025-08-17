package akkimi_BE.aja.service.auth;

import akkimi_BE.aja.dto.oauth.KakaoTokenResponse;
import akkimi_BE.aja.dto.oauth.KakaoUserInfo;
import akkimi_BE.aja.dto.response.TokenResponse;
import akkimi_BE.aja.entity.Role;
import akkimi_BE.aja.entity.SocialType;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import global.exception.CustomException;
import global.exception.HttpErrorCode;
import global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OAuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public TokenResponse kakaoLogin(String code) {
        log.info("카카오 로그인 시작");

        // 1. 인가 코드로 액세스 토큰 받기
        KakaoTokenResponse kakaoToken = getKakaoToken(code);
        log.info("카카오 액세스 토큰 획득 성공");

        // 2. 액세스 토큰으로 사용자 정보 가져오기
        Map<String, Object> userInfo = getKakaoUserInfo(kakaoToken.getAccessToken());
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(userInfo);

        // 3. 사용자 저장
        User user = save(kakaoUserInfo);
        log.info("사용자 정보 저장 완료: {}", user.getNickname());

        // 4. JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getSocialId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getSocialId());
        log.info("JWT 토큰 생성 완료");

        // 5. RT 저장 (고정 정책) — 단일 디바이스만 허용하고 싶으면 revokeAll(user) 후 저장
        refreshTokenService.store(user, refreshToken, jwtUtil.getRefreshExpirySeconds());

        // 6. 응답 생성
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
                        .role(user.getRole().name())
                        .socialType(user.getSocialType().name())
                        .build())
                .build();
    }

    private KakaoTokenResponse getKakaoToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    KAKAO_TOKEN_URL,
                    request,
                    String.class
            );

            return objectMapper.readValue(response.getBody(), KakaoTokenResponse.class);
        } catch (Exception e) {
            log.error("카카오 토큰 요청 실패: {}", e.getMessage());
            throw new RuntimeException("카카오 토큰 획득 실패", e);
        }
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패: {}", e.getMessage());
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }

    private User save(KakaoUserInfo kakaoUserInfo) {
        String socialId = kakaoUserInfo.getId();

        return userRepository.findBySocialId(socialId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .socialId(socialId)
                            .socialType(SocialType.KAKAO)
                            .role(Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    // 토큰 재발급: RT 검증/저장 확인 → Access만 새발급 (RT는 유지)
    public TokenResponse refresh(String refreshToken) {

        // 1) RT 유효성 검증 (서명/만료)
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(HttpErrorCode.UNAUTHORIZED); // 적절한 에러코드로 치환
        }
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new CustomException(HttpErrorCode.UNAUTHORIZED);
        }

        // 2) 서버 저장소에서 RT가 활성 상태인지 확인(탈취/로그아웃/블랙리스트 방지)
        var stored = refreshTokenService.find(refreshToken)
                .orElseThrow(() -> new CustomException(HttpErrorCode.UNAUTHORIZED));

        if (stored.isExpired(LocalDateTime.now())) {
            refreshTokenService.revoke(refreshToken);
            throw new CustomException(HttpErrorCode.UNAUTHORIZED);
        }

        // 3) 토큰 재발급
        String socialId = jwtUtil.getSocialIdFromRefresh(refreshToken);
        User user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new CustomException(HttpErrorCode.SOCIALID_NOT_FOUND));

        String newAccess = jwtUtil.issueAccessToken(user);

        return TokenResponse.builder()
                .success(true)
                .message("토큰 재발급 성공")
                .accessToken(newAccess)
                .refreshToken(refreshToken) // 고정 RT 정책: 그대로 반환
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .user(TokenResponse.UserInfo.builder()
                        .userId(user.getUserId())
                        .socialId(user.getSocialId())
                        .nickname(user.getNickname())
                        .role(user.getRole().name())
                        .socialType(user.getSocialType().name())
                        .build())
                .build();
    }

    // 로그아웃: 서버 저장 RT 폐기(또는 블랙리스트 등록)
    // Access Token은 stateless 특성상 즉시 무효화 불가(만료 대기)
    public boolean logout(Authentication auth, String refreshToken) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new CustomException(HttpErrorCode.UNAUTHORIZED);
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(HttpErrorCode.REFRESH_TOKEN_NULL);
        }

        // 만료/서명오류여도 멱등 응답을 위해 true 반환 가능
        if (jwtUtil.validateRefreshToken(refreshToken)) {
            refreshTokenService.revoke(refreshToken);
        }
        return true;
    }
}