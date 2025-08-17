package akkimi_BE.aja.controller;


import akkimi_BE.aja.dto.oauth.KakaoLoginRequest;
import akkimi_BE.aja.dto.request.*;
import akkimi_BE.aja.dto.response.TokenResponse;
import akkimi_BE.aja.dto.response.TokenValidationResponseDto;
import akkimi_BE.aja.dto.response.UserResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.OAuthService;
import akkimi_BE.aja.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final OAuthService oAuthService;
    private final UserService userService;

    @PostMapping("/kakao")
    @Operation(security = @SecurityRequirement(name = "")) //swagger 인증 불필요한 예외 처리
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
        User user = (User) authentication.getPrincipal(); // principal이 이제 User 객체이므로 직접 가져옴
        return UserResponseDto.from(user);
    }

    @GetMapping("/validate")
    public TokenValidationResponseDto validateToken(Authentication authentication) {
        return TokenValidationResponseDto.builder()
                .valid(true)
                .userSocialId(authentication.getName()) //getName이 socialId
                .authorities(authentication.getAuthorities())
                .authenticated(authentication.isAuthenticated())
                .build();
    }

    @PostMapping("/refresh")
    @Operation(security = @SecurityRequirement(name = "")) //swagger 인증 불필요한 예외 처리
    public TokenResponse refresh(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return oAuthService.refresh(refreshTokenRequestDto.getRefreshToken());
    }

    @PostMapping("/logout")
    public Boolean logout(Authentication authentication, @RequestBody LogoutRequestDto logoutRequestDto) {
        return oAuthService.logout(authentication, logoutRequestDto.getRefreshToken());
    }
    
    @GetMapping("/callback")
    @Operation(security = @SecurityRequirement(name = ""))
    public String kakaoCallback() {
        // 정적 HTML 파일로 리다이렉트
        return "redirect:/callback.html";
    }


    /* 회원가입 */
    @PostMapping("/signup/email")
    public Long signupWithEmail(@RequestBody EmailRequestDto emailRequestDto) {
        return userService.signupWithEmail(emailRequestDto);
    }

    @PostMapping("/signup/phone")
    public Long signupWithPhone(@RequestBody PhoneRequestDto phoneSignupRequestDto) {
        return userService.signupWithPhone(phoneSignupRequestDto);
    }

    /* 로그인 */
    @PostMapping("/login/email")
    public TokenResponse loginWithEmail(@RequestBody EmailRequestDto emailLoginRequestDto) {
        return userService.loginWithEmail(emailLoginRequestDto);
    }

    @PostMapping("/login/phone")
    public TokenResponse loginWithPhone(@RequestBody PhoneRequestDto phoneLoginRequestDto) {
        return userService.loginWithPhone(phoneLoginRequestDto);
    }

    /* 중복 확인 */
    @PostMapping("/validate/phone")
    public Map<String, Boolean> validatePhone(@RequestBody PhoneValidateRequestDto phoneValidateRequestDto) {
        Boolean result = userService.validatePhone(phoneValidateRequestDto);
        return Map.of("available", result);
    }

    @PostMapping("/validate/email")
    public Map<String, Boolean> validateEmail(@RequestBody EmailValidateRequestDto emailValidateRequestDto) {
        Boolean result = userService.validateEmail(emailValidateRequestDto);
        return Map.of("available", result);

    }

}