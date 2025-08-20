package akkimi_BE.aja.controller;


import akkimi_BE.aja.dto.auth.KakaoLoginRequest;
import akkimi_BE.aja.dto.auth.LogoutRequestDto;
import akkimi_BE.aja.dto.auth.RefreshTokenRequestDto;
import akkimi_BE.aja.dto.auth.TokenResponse;
import akkimi_BE.aja.dto.auth.TokenValidationResponseDto;
import akkimi_BE.aja.dto.request.*;
import akkimi_BE.aja.dto.response.UserResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.auth.OAuthService;
import akkimi_BE.aja.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "사용자 인증 관련 API")
public class AuthController {

    private final OAuthService oAuthService;
    private final UserService userService;

    @PostMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 OAuth를 통한 로그인")
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
    @Operation(hidden = true)
    @SecurityRequirement(name = "bearerAuth")
    public UserResponseDto getCurrentUserInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal(); // principal이 이제 User 객체이므로 직접 가져옴
        return UserResponseDto.from(user);
    }

    //Todo 온보딩 여부 포함시키기
    @GetMapping("/validate")
    @Operation(summary = "토큰 유효성 검증", description = "JWT 토큰의 유효성을 검증합니다")
    @SecurityRequirement(name = "bearerAuth")
    public TokenValidationResponseDto validateToken(Authentication authentication) {
        return TokenValidationResponseDto.builder()
                .valid(true)
                .userSocialId(authentication.getName()) //getName이 socialId
                .authorities(authentication.getAuthorities())
                .authenticated(authentication.isAuthenticated())
                .build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급합니다")
    public TokenResponse refresh(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        return oAuthService.refresh(refreshTokenRequestDto.getRefreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리")
    @SecurityRequirement(name = "bearerAuth")
    public Boolean logout(Authentication authentication, @RequestBody LogoutRequestDto logoutRequestDto) {
        return oAuthService.logout(authentication, logoutRequestDto.getRefreshToken());
    }

    /* 회원가입 */
    @PostMapping("/signup/email")
    @Operation(summary = "이메일 회원가입", description = "이메일을 통한 회원가입")
    public TokenResponse signupWithEmail(@RequestBody EmailRequestDto emailRequestDto) {
        return userService.signupWithEmail(emailRequestDto);
    }

    @PostMapping("/signup/phone")
    @Operation(summary = "전화번호 회원가입", description = "전화번호를 통한 회원가입")
    public TokenResponse signupWithPhone(@RequestBody PhoneRequestDto phoneSignupRequestDto) {
        return userService.signupWithPhone(phoneSignupRequestDto);
    }

    /* 로그인 */
    @PostMapping("/login/email")
    @Operation(summary = "이메일 로그인", description = "이메일을 통한 로그인")
    public TokenResponse loginWithEmail(@RequestBody EmailRequestDto emailLoginRequestDto) {
        return userService.loginWithEmail(emailLoginRequestDto);
    }

    @PostMapping("/login/phone")
    @Operation(summary = "전화번호 로그인", description = "전화번호를 통한 로그인")
    public TokenResponse loginWithPhone(@RequestBody PhoneRequestDto phoneLoginRequestDto) {
        return userService.loginWithPhone(phoneLoginRequestDto);
    }

    /* 중복 확인 */
    @PostMapping("/validate/phone")
    @Operation(summary = "전화번호 중복 확인", description = "전화번호 사용 가능 여부를 확인합니다")
    public Map<String, Boolean> validatePhone(@RequestBody PhoneValidateRequestDto phoneValidateRequestDto) {
        Boolean result = userService.validatePhone(phoneValidateRequestDto);
        return Map.of("available", result);
    }

    @PostMapping("/validate/email")
    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다")
    public Map<String, Boolean> validateEmail(@RequestBody EmailValidateRequestDto emailValidateRequestDto) {
        Boolean result = userService.validateEmail(emailValidateRequestDto);
        return Map.of("available", result);

    }

    @DeleteMapping("/withdrawal")
    @Operation(summary = "회원 탈퇴", description = "사용자 계정을 완전히 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    public Map<String, String> withdrawUser(@AuthenticationPrincipal User user) {
        userService.withdrawUser(user);
        return Map.of("message", "회원 탈퇴가 완료되었습니다");
    }

}