package akkimi_BE.aja.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PhoneRequestDto {
    @Size(min = 13, max = 13)
    private String phoneNumber;
    private String password;
}
