package akkimi_BE.aja.dto.response;

import akkimi_BE.aja.entity.SavingGoal;
import lombok.*;

import java.time.LocalDate;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SavingGoalResponseDto {
    private Long goalId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dDayLabel;
    private long dDay;

    public static SavingGoalResponseDto from(SavingGoal g) {
        return SavingGoalResponseDto.builder()
                .goalId(g.getGoalId())
                .startDate(LocalDate.from(g.getStartDate()))
                .endDate(LocalDate.from(g.getEndDate()))
                .dDayLabel(g.getDDayLabel())
                .dDay(g.getDDay())
                .build();
    }
}

