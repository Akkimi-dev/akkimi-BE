package akkimi_BE.aja.global.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record CustomErrorResponse(
        boolean success,  // 항상 false
        int status, // HTTP 상태
        String message, // 에러 메시지
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
    public static CustomErrorResponse of(int status, String message) {
        return new CustomErrorResponse(false, status, message, LocalDateTime.now());
    }
}
