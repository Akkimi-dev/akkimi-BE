package akkimi_BE.aja.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatHistoryResponseDto {
    private List<MessageDto> messages;   // 시간 오름차순
    private boolean hasMore;       // 더 불러올 게 있는가
    private Long nextBeforeId;     // 다음 요청 시 beforeId로 사용

    @Getter @Builder
    public static class MessageDto {
        private Long chatId;
        private String speaker;    // USER / BOT
        private String message;
        private String createdAt;  // ISO-8601
        private boolean showDate;
        private String dateLabel;  // yyyy.MM.dd (E)
        private boolean showTime;
        private String timeLabel;  // HH:mm
    }
}
