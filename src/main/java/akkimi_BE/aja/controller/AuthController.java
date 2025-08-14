package akkimi_BE.aja.controller;


import akkimi_BE.aja.dto.oauth.KakaoLoginRequest;
import akkimi_BE.aja.dto.request.LogoutRequestDto;
import akkimi_BE.aja.dto.request.RefreshTokenRequestDto;
import akkimi_BE.aja.dto.response.TokenResponse;
import akkimi_BE.aja.dto.response.TokenValidationResponseDto;
import akkimi_BE.aja.dto.response.UserResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.OAuthService;
import akkimi_BE.aja.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final OAuthService oAuthService;
    private final UserService userService;

    @PostMapping("/kakao")
    public TokenResponse kakaoLogin(@RequestBody KakaoLoginRequest request) {
        log.info("카카오 로그인 요청 - 인가 코드: {}",
                request.getCode().substring(0, Math.min(request.getCode().length(), 10)) + "...");

        try {
            TokenResponse tokenResponse = oAuthService.kakaoLogin(request.getCode());
            log.info("카카오 로그인 성공 - 사용자: {}", tokenResponse.getUser().getNickname());
            return tokenResponse;
        } catch (Exception e) {
            log.error("카카오 로그인 실패: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/me")
    public UserResponseDto getCurrentUserInfo(Authentication authentication) {
        User user = userService.findBySocialId(authentication.getName()); // authentication.getName()은 JwtFilter에서 설정한 socialId
        return UserResponseDto.from(user);
    }

    @GetMapping("/validate")
    public TokenValidationResponseDto validateToken(Authentication authentication) {
        return TokenValidationResponseDto.builder()
                .valid(true)
                .userSocialId(authentication.getName())
                .authorities(authentication.getAuthorities())
                .authenticated(authentication.isAuthenticated())
                .build();
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return oAuthService.refresh(refreshTokenRequestDto.getRefreshToken());
    }

    @PostMapping("/logout")
    public Boolean logout(Authentication authentication, @RequestBody LogoutRequestDto logoutRequestDto) {
        return oAuthService.logout(authentication, logoutRequestDto.getRefreshToken());
    }
    
    @GetMapping("/callback")
    public String kakaoCallback() {
        // 정적 HTML 파일로 리다이렉트
        return "redirect:/callback.html";
    }
}