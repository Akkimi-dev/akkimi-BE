package global.exception;

import global.response.CustomErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice(basePackages = "akkimi_BE.aja.controller")
public class GlobalExceptionHandler {

    // 도메인 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomErrorResponse> handleCustom(CustomException ex) {
        var ec = ex.getErrorCode();
        log.warn("[BUSINESS] {}", ex.getMessage());
        var body = CustomErrorResponse.of(ec.getHttpStatus().value(), ex.getMessage());
        return ResponseEntity.status(ec.getHttpStatus()).body(body);
    }

    /* 요청 검증/바인딩 */
    // 쿼리스트링/폼 파라미터 바인딩(@ModelAttribute 등)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<CustomErrorResponse> handleBind(BindException ex) {
        var msg = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("요청 파라미터 검증에 실패했습니다.");
        log.debug("[BIND] {}", msg);
        return ResponseEntity.badRequest().body(CustomErrorResponse.of(400, msg));
    }

    // 검증 실패(@Valid @RequestBody DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> String.format("[%s] %s", err.getField(), err.getDefaultMessage()))
                .orElse("요청 데이터 검증에 실패했습니다.");
        log.debug("[VALID] {}", msg);
        return ResponseEntity.badRequest().body(CustomErrorResponse.of(400, msg));
    }

    // 타입 변환 실패 (e.g. /users/{id}에 문자열)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var msg = "요청 값 타입이 올바르지 않습니다.";
        log.debug("[TYPE MISMATCH] param={}, value={}", ex.getName(), ex.getValue());
        return ResponseEntity.badRequest().body(CustomErrorResponse.of(400, msg));
    }

    // 필수 요청 파라미터 누락 시
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.debug("[MISSING PATHVAR] 필수 경로 변수가 누락되었습니다");
        return ResponseEntity.badRequest().body(CustomErrorResponse.of(400, "필수 경로 변수가 누락되었습니다"));
    }

    // 요청 바디가 JSON 등으로 파싱 실패 시
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.debug("[NOT READABLE] {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return ResponseEntity.badRequest().body(CustomErrorResponse.of(400, "요청 본문을 읽을 수 없습니다."));
    }

    /* HTTP 프로토콜 레벨 */
    // 허용하지 않는 HTTP 메서드 요청 시
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.debug("[METHOD NOT ALLOWED] {}", ex.getMethod());
        return ResponseEntity.status(METHOD_NOT_ALLOWED)
                .body(CustomErrorResponse.of(METHOD_NOT_ALLOWED.value(), "허용되지 않는 HTTP 메서드입니다."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CustomErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.debug("[UNSUPPORTED MEDIA TYPE] {}", ex.getContentType());
        return ResponseEntity.status(UNSUPPORTED_MEDIA_TYPE)
                .body(CustomErrorResponse.of(UNSUPPORTED_MEDIA_TYPE.value(), "지원하지 않는 Content-Type 입니다."));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<CustomErrorResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        log.debug("[NOT ACCEPTABLE] {}", ex.getMessage());
        return ResponseEntity.status(NOT_ACCEPTABLE)
                .body(CustomErrorResponse.of(NOT_ACCEPTABLE.value(), "응답 형식을 협상할 수 없습니다."));
    }

    /* 보안 */
    // 인증 실패
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomErrorResponse> handleAuth(AuthenticationException ex) {
        log.debug("[AUTHENTICATION] {}", ex.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(CustomErrorResponse.of(401, "인증이 필요합니다."));
    }

    // 인가 실패(권한 없음)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.debug("[ACCESS DENIED] {}", ex.getMessage());
        return ResponseEntity.status(FORBIDDEN).body(CustomErrorResponse.of(403, "접근 권한이 없습니다."));
    }

    // DB 제약조건 위반 등 충돌 시
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CustomErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("[DATA INTEGRITY] {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return ResponseEntity.status(CONFLICT)
                .body(CustomErrorResponse.of(CONFLICT.value(), "데이터 제약 조건을 위반했습니다."));
    }

    // 그 외 알 수 없는 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleUnknown(Exception ex) {
        log.error("[INTERNAL ERROR]", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(CustomErrorResponse.of(500, "요청 처리 중 알 수 없는 오류가 발생했습니다."));
    }
}