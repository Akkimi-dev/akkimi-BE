package akkimi_BE.aja.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DayConsumptionSummaryDto {

    private LocalDate date;     // 조회일
    private int total;          // 항목 개수
    private int sum;            // 금액 합계
    private List<Item> summary; // 항목 요약

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Item {
        private Long consumptionId;
        private Integer price;
    }
}