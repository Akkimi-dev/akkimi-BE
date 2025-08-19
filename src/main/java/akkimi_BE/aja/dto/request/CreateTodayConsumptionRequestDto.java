package akkimi_BE.aja.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateTodayConsumptionRequestDto {
    @NotBlank  private String category;
    @NotBlank  private String itemName;
    @NotBlank @Min(0) private Integer amount;
    private  String description;
}
