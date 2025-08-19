package akkimi_BE.aja.dto.request;

import lombok.Getter;

import javax.validation.constraints.Min;

@Getter
public class UpdateTodayConsumptionDto {
    private String category;
    private String itemName;
    @Min(0) private Integer amount;
    private String description;
}
