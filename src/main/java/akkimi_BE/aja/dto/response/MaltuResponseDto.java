package akkimi_BE.aja.dto.response;

import akkimi_BE.aja.entity.Maltu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaltuResponseDto {
    private Long maltuId;
    private Long creatorId;
    private String creatorName;
    private String maltuName;
    private Boolean isPublic;
    private String prompt;

    public static MaltuResponseDto from(Maltu maltu) {
        return MaltuResponseDto.builder()
                .maltuId(maltu.getMaltuId())
                .creatorId(maltu.getCreator().getUserId())
                .creatorName(maltu.getCreator().getNickname())
                .maltuName(maltu.getMaltuName())
                .isPublic(maltu.getIsPublic())
                .prompt(maltu.getPrompt())
                .build();
    }
}