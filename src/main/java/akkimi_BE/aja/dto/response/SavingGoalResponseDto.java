package akkimi_BE.aja.dto.response;

import akkimi_BE.aja.entity.SavingGoal;
import lombok.*;

import java.time.LocalDate;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SavingGoalResponseDto {
    private Long goalId;
    private Long userId;
    private String purpose;
    private Integer purposeBudget;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCompleted;
    private Integer totalSum;
    private String dDayLabel;
    private long dDay;

    public static SavingGoalResponseDto from(SavingGoal g) {
        return SavingGoalResponseDto.builder()
                .goalId(g.getGoalId())
                .userId(g.getUser().getUserId())
                .purpose(g.getPurpose())
                .purposeBudget(g.getPurposeBudget())
                .startDate(LocalDate.from(g.getStartDate()))
                .endDate(LocalDate.from(g.getEndDate()))
                .totalSum(g.getTotalSum() == null ? 0 : g.getTotalSum())
                .dDayLabel(g.getDDayLabel())
                .dDay(g.getDDay())
                .build();
    }
}

