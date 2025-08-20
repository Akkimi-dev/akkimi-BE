package akkimi_BE.aja.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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