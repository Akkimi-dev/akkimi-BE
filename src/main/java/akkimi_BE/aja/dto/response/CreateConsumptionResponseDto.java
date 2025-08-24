package akkimi_BE.aja.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreateConsumptionResponseDto {
    private Long consumptionId;   // 생성된 소비내역 ID
    private String feedback;      // 챗봇 피드백 메시지
}
