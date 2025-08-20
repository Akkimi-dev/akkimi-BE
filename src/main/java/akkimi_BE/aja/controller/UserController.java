package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.request.UpdateNicknameRequestDto;
import akkimi_BE.aja.dto.request.UpdateRegionRequestDto;
import akkimi_BE.aja.dto.response.CurrentMaltuResponseDto;
import akkimi_BE.aja.dto.response.UserProfileResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "유저 API")
public class UserController {
    private final UserService userService;

    @GetMapping("/setup")
    @Operation(summary = "isSetup 확인", description = "isSetup 프로필 조회, 소비 캐릭터 설정에도 있습니다.")
    @SecurityRequirement(name = "bearerAuth")
    public Map<String, Boolean> getIsSetup(@AuthenticationPrincipal User user) {
        Boolean isSetup = userService.getIsSetup(user);
        return Map.of("isSetup", isSetup);
    }


    @GetMapping
    @Operation(summary = "사용자 프로필 조회", description = "사용자의 프로필 정보를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public UserProfileResponseDto getUserProfile(@AuthenticationPrincipal User user) {
        return userService.getUserProfile(user);
    }


    @PutMapping("/character/{characterId}")
    @Operation(summary = "소비 캐릭터 설정 및 수정", description = "사용자의 소비 캐릭터를 설정합니다")
    @SecurityRequirement(name = "bearerAuth")
    public Map<String, Boolean> updateCharacter(@AuthenticationPrincipal User user,
                                                @PathVariable Long characterId) {
        Boolean isSetup = userService.updateCharacter(user, characterId);
        return Map.of("isSetup", isSetup);
    }

    @PutMapping("/nickname")
    @Operation(summary = "닉네임 설정 및 수정", description = "사용자의 닉네임을 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    public void updateNickname(@AuthenticationPrincipal User user,
                               @RequestBody @Valid UpdateNicknameRequestDto updateNicknameRequestDto) {
        userService.updateNickname(user, updateNicknameRequestDto.getNickname());
    }

    @PutMapping("/region")
    @Operation(summary = "지역 설정 및 수정", description = "사용자의 지역을 설정합니다")
    @SecurityRequirement(name = "bearerAuth")
    public void updateRegion(@AuthenticationPrincipal User user,
                             @RequestBody UpdateRegionRequestDto request) {
        userService.updateRegion(user, request.getRegion());
    }

    @PutMapping("/current-maltu/{maltuId}")
    @Operation(summary = "현재 말투 설정 및 수정", description = "사용자의 현재 사용 중인 말투를 변경합니다")
    @SecurityRequirement(name = "bearerAuth")
    public void updateCurrentMaltu(@AuthenticationPrincipal User user,
                                   @PathVariable Long maltuId) {
        userService.updateCurrentMaltu(user, maltuId);
    }

    @GetMapping("/current-maltu")
    @Operation(summary = "현재 말투 조회", description = "사용자의 현재 사용 중인 말투를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public CurrentMaltuResponseDto getCurrentMaltu(@AuthenticationPrincipal User user) {
        return userService.getCurrentMaltu(user);
    }
}
