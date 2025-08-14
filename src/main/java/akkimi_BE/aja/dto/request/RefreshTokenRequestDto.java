package akkimi_BE.aja.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenRequestDto {
    private String refreshToken;
}
