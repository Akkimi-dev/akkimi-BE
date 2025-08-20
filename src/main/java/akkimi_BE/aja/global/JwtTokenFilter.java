package akkimi_BE.aja.global;

import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.UserRepository;
import akkimi_BE.aja.global.util.JwtUtil;
import akkimi_BE.aja.global.exception.AuthenticationErrorCode;
import akkimi_BE.aja.global.exception.CustomException;
import akkimi_BE.aja.global.response.CustomErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("JwtTokenFilter Bean이 생성되었습니다!");
        System.out.println("JwtUtil 주입 확인: " + (jwtUtil != null ? "성공" : "실패"));
        System.out.println("UserRepository 주입 확인: " + (userRepository != null ? "성공" : "실패"));
        System.out.println("========================================");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //에러 때문에 추가한 코드
        String requestURI = request.getRequestURI();
        System.out.println("===== JwtTokenFilter 실행됨 =====");
        System.out.println("Request URI: " + requestURI);
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));
        log.info("필터 통과 요청 URI: {}", requestURI);

        // Swagger 경로는 JWT 인증 필터에서 예외 처리
        if (requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/swagger-ui")
                || requestURI.startsWith("/swagger-resources")
                || requestURI.startsWith("/webjars")) {
            log.info("swagger 예외 처리됨 (필터 통과): {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // OAuth2 콜백 경로는 JWT 인증 필터에서 예외 처리
        if (requestURI.equals("/auth/kakao") ||
                requestURI.equals("/auth/callback") ||
                requestURI.startsWith("/api/oauth/")) {
            log.info("oauth2 콜백 경로 예외 처리됨 (필터 통과): {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }


        //여기까지

        String token = getTokenFromRequest(request);

        // 토큰이 없는 경우 - 다음 필터로 진행 (공개 API 접근 가능)
        // 토큰 없는데 인증을 하는 경우 spring security에서 403 Forbidden
        if(token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 있는 경우 - 검증 수행
        try {
            jwtUtil.validateToken(token);
            
            // 토큰이 유효한 경우 - 인증 정보 설정
            String socialId = jwtUtil.getSocialIdFromToken(token);
            if(socialId != null) {
                Optional<User> user = userRepository.findBySocialId(socialId);
                if(user.isPresent()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.get(),  // principal을 User 객체로 설정
                                    null,
                                    user.get().getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 토큰은 유효하지만 사용자를 찾을 수 없는 경우
                    log.warn("토큰의 사용자를 찾을 수 없음: socialId={}", socialId);
                    sendErrorResponse(response, new CustomException(AuthenticationErrorCode.USER_NOT_FOUND_BY_TOKEN));
                    return;
                }
            }
        } catch (CustomException e) {
            // 토큰 검증 실패 시 바로 에러 응답 반환
            log.warn("토큰 검증 실패: {}", e.getMessage());
            sendErrorResponse(response, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Authorization 헤더가 존재하고 "Bearer "로 시작하는지 확인
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 부분을 제거하고 토큰만 반환
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * CustomException을 받아서 에러 응답을 직접 생성하여 반환
     * @param response HttpServletResponse 객체
     * @param exception CustomException 객체
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, CustomException exception) throws IOException {
        int statusCode = exception.getErrorCode().getHttpStatus().value();
        String errorMessage = exception.getMessage();
        
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        CustomErrorResponse errorResponse = CustomErrorResponse.of(statusCode, errorMessage);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}



