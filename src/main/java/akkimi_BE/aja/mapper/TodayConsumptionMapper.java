package akkimi_BE.aja.mapper;

import akkimi_BE.aja.dto.response.TodayConsumptionResponseDto;
import akkimi_BE.aja.entity.TodayConsumption;

public final class TodayConsumptionMapper {

    private TodayConsumptionMapper() {}

    public static TodayConsumptionResponseDto toDto(TodayConsumption c) {
        return TodayConsumptionResponseDto.builder()
                .consumptionId(c.getConsumptionId())
                .todayDateId(c.getTodayDate().getTodayDateId())
                .goalId(c.getTodayDate().getGoal().getGoalId())
                .date(c.getTodayDate().getTodayDate())
                .category(c.getCategory())
                .itemName(c.getItemName())
                .amount(c.getAmount())
                .description(c.getDescription())
                .build();
    }
}
