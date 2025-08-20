package akkimi_BE.aja.dto.response;

import akkimi_BE.aja.entity.Character;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CharacterResponseDto {
    private Long id;
    private String name;

    public static CharacterResponseDto from(Character character) {
        return CharacterResponseDto.builder()
                .id(character.getCharacterId())
                .name(character.getCharacterName())
                .build();
    }
}