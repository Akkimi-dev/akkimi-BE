package akkimi_BE.aja.dto.response;

import akkimi_BE.aja.entity.Character;
import akkimi_BE.aja.entity.SocialType;
import akkimi_BE.aja.entity.User;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDto {
    private Long userId;
    private String nickname;
    private CharacterDto character;
    private String region;
    private String email;
    private String phoneNumber;
    private SocialType socialType;
    private Boolean isSetup;

    public static UserProfileResponseDto from(User user, Character character) {
        // Character -> CharacterDto 변환
        CharacterDto characterDto = null;
        if (character != null) {
            characterDto = CharacterDto.builder()
                    .id(character.getCharacterId())
                    .name(character.getCharacterName())
                    .build();
        }

        // 반드시 return!
        return UserProfileResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .character(characterDto)
                .region(user.getRegion())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .socialType(user.getSocialType())
                .isSetup(user.getIsSetup())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CharacterDto {
        private Long id;
        private String name;
    }
}