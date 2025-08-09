package global.exception;

import global.response.CustomErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스/인가 등 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomErrorResponse> handleCustom(CustomException ex) {
        var ec = ex.getErrorCode();
        var body = CustomErrorResponse.of(ec.getHttpStatus().value(), ec.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ec.getHttpStatus()).body(body);
    }

    // 검증 실패(@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var body = CustomErrorResponse.of(400, 2000, "요청 데이터 검증에 실패했습니다.");
        return ResponseEntity.badRequest().body(body);
    }

    // 그 외 알 수 없는 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleUnknown(Exception ex) {
        var ec = HttpErrorCode.INTERNAL_SERVER_ERROR;
        var body = CustomErrorResponse.of(ec.getHttpStatus().value(), ec.getErrorCode(), ec.getErrorMessage());
        return ResponseEntity.status(ec.getHttpStatus()).body(body);
    }
}