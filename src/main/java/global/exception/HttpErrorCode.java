package global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HttpErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR("요청 처리 중 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("인증이 필요한 요청입니다.", HttpStatus.UNAUTHORIZED);

    private final String errorMessage; // 에러 메시지
    private final HttpStatus httpStatus; // 매핑할 HTTP 상태
}
