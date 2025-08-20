package akkimi_BE.aja.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthenticationErrorCode implements ErrorCode {

    TOKEN_NOT_VALID("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED), //401
    EXPIRED_TOKEN("토큰이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    TOKEN_SIGNATURE_ERROR("토큰 서명이 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_MALFORMED("잘못된 형식의 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_UNSUPPORTED("지원되지 않는 토큰 형식입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EMPTY("토큰이 비어있습니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND_BY_TOKEN("토큰의 사용자 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_PARSING_ERROR("토큰 파싱 중 오류가 발생했습니다.", HttpStatus.UNAUTHORIZED);

    private final String errorMessage;
    private final HttpStatus httpStatus;
}