package akkimi_BE.aja.global.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record CommonResponse<T>(
        boolean success,   // 성공 여부
        int status,        // HTTP 상태 코드 (200, 201 등)
        String message,    // 사용자/개발자용 메시지
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        T result          // 실제 데이터
) {
    // 성공 + 데이터 O
    public static <T> CommonResponse<T> of(int status, String message, T result) {
        return new CommonResponse<>(true, status, message, LocalDateTime.now(), result);
    }
    // 성공 + 데이터 X
    public static <T> CommonResponse<T> success(int status, String message) {
        return new CommonResponse<>(true, status, message, LocalDateTime.now(), null);
    }
}
