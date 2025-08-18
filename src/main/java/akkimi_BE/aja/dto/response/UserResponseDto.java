package akkimi_BE.aja.dto.response;

import akkimi_BE.aja.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto  {
    private Long userId;
    private String socialId;
    private String email;
    private String phoneNumber;
    private String nickname;
    private String role;
    private String socialType;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .socialId(user.getSocialId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .socialType(user.getSocialType().name())
                .build();
    }
}

