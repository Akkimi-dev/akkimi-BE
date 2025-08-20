package akkimi_BE.aja.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonthConsumptionSummaryDto {

    private YearMonth month;       // 조회 월 (yyyy-MM)
    private List<DateSummary> summary; // 날짜별 합계

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class DateSummary {
        private String date;   // yyyy-MM-dd
        private Integer price; // 합계
    }
}