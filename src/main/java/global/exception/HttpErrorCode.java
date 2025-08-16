package global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HttpErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR("요청 처리 중 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("인증이 필요한 요청입니다.", HttpStatus.UNAUTHORIZED),
    SOCIALID_NOT_FOUND("해당 socialId로 회원을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_NULL("Refresh Token의 값이 없습니다.",HttpStatus.BAD_REQUEST),
    BAD_REQUSET("잘못된 요청입니다.",HttpStatus.BAD_REQUEST),
    NOT_FOUND("대상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONFLICT("요청이 충돌했습니다.", HttpStatus.CONFLICT);

    private final String errorMessage; // 에러 메시지
    private final HttpStatus httpStatus; // 매핑할 HTTP 상태
}
