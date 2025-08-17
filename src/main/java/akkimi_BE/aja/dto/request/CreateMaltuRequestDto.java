package akkimi_BE.aja.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaltuRequestDto {
    private String maltuName;
    private Boolean isPublic;
    private String prompt;
}