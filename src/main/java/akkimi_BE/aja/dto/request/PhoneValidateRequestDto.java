package akkimi_BE.aja.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PhoneValidateRequestDto {
    @Size(min = 13, max = 13)
    private String phoneNumber;
}
