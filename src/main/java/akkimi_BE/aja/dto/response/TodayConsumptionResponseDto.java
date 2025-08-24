package akkimi_BE.aja.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class TodayConsumptionResponseDto {
    private Long consumptionId;
    private Long todayDateId;
    private Long goalId;

    private LocalDate date;

    private String category;
    private String itemName;
    private Integer amount;
    private String description;
}