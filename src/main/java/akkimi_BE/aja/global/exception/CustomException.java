package akkimi_BE.aja.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    
    @Override
    public String getMessage() {
        return errorCode.getErrorMessage();
    }
}
