package akkimi_BE.aja.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class EmailRequestDto {
    @Email
    private String email;
    private String password;
}
