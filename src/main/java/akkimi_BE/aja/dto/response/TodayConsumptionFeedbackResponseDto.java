package akkimi_BE.aja.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodayConsumptionFeedbackResponseDto {

    private Long consumptionId;
    private Long todayDateId;
    private Long goalId;

    private LocalDate date;

    private String category;
    private String itemName;
    private Integer amount;
    private String description;
    private ChatMessageDto feedback;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessageDto {
        private Long messageId;
        private String feedback;
    }
}
