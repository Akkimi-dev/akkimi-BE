package akkimi_BE.aja.dto.response;

import akkimi_BE.aja.entity.Maltu;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentMaltuResponseDto {
    private Long maltuId;
    private String maltuName;
    private Boolean isPublic;
    private String prompt;

    public static CurrentMaltuResponseDto from(Maltu maltu) {
        return CurrentMaltuResponseDto.builder()
                .maltuId(maltu.getMaltuId())
                .maltuName(maltu.getMaltuName())
                .isPublic(maltu.getIsPublic())
                .prompt(maltu.getPrompt())
                .build();
    }
}
