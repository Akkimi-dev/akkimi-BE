package akkimi_BE.aja.dto.auth;

import lombok.Getter;
import java.util.Map;

@Getter
public class KakaoUserInfo {
    private String id;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.id = String.valueOf(attributes.get("id"));
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
    }
}


