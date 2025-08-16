package akkimi_BE.aja.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UpdateSavingGoalRequestDto {

    @Size(max = 50, message = "목표이름은 50자를 초과할 수 없습니다.")
    private String purpose;

    @Positive(message = "목표예산은 0보다 커야합니다.")
    private Integer purposeBudget;

    private LocalDate startDate;
    private LocalDate endDate;


}
