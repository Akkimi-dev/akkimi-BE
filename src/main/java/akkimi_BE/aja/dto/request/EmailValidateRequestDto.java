package akkimi_BE.aja.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class EmailValidateRequestDto {
    @Email
    private String email;
}
