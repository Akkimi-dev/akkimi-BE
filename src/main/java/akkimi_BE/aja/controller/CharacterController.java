package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.response.CharacterResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
@Tag(name = "소비 성향 캐릭터 API")
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping
    @Operation(summary = "전체 캐릭터 목록 조회", description = "모든 캐릭터 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public List<CharacterResponseDto> getAllCharacters(@AuthenticationPrincipal User user) {
        return characterService.getAllCharacters();
    }

    @GetMapping("/{characterId}")
    @Operation(summary = "캐릭터 조회", description = "특정 캐릭터 id의 이름을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public CharacterResponseDto getCharacter(@AuthenticationPrincipal User user, @PathVariable Long characterId) {
        return characterService.getCharacter(characterId);
    }
}