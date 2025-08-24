package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.request.CreateMaltuRequestDto;
import akkimi_BE.aja.dto.request.UpdateMaltuRequestDto;
import akkimi_BE.aja.dto.response.MaltuResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.MaltuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maltus")
@Tag(name = "말투 API")
public class MaltuController {
    private final MaltuService maltuService;

    @PostMapping
    @Operation(summary = "말투 생성", description = "새로운 말투를 생성합니다")
    @SecurityRequirement(name = "bearerAuth")
    public Long createMaltu(@AuthenticationPrincipal User user, @RequestBody CreateMaltuRequestDto createMaltuRequestDto) {
        return maltuService.createMaltu(user, createMaltuRequestDto);
    }

    @GetMapping("/{maltuId}")
    @Operation(summary = "말투 상세 조회", description = "특정 말투의 상세 정보를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public MaltuResponseDto getMaltu(@AuthenticationPrincipal User user, @PathVariable Long maltuId) {
        return maltuService.getMaltu(user, maltuId);
    }

    @GetMapping("/public/list")
    @Operation(summary = "공개 말투 목록 조회(기본 말투 포함o)", description = "다른 사용자가 공유한 말투 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public List<MaltuResponseDto> getPublicMaltus(@AuthenticationPrincipal User user) {
        return maltuService.getPublicMaltus();
    }

    @GetMapping("/mine/list")
    @Operation(summary = "내 말투 목록 조회", description = "사용자가 생성한 말투 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public List<MaltuResponseDto> getMyMaltus(@AuthenticationPrincipal User user) {
        return maltuService.getMyMaltus(user);
    }

    @GetMapping("/default")
    @Operation(summary = "기본 말투 목록 조회", description = "시스템에서 제공하는 기본 말투 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public List<MaltuResponseDto> getDefaultMaltu(@AuthenticationPrincipal User user) {
        return maltuService.getDefaultMaltus();
    }

    @PatchMapping("/{maltuId}")
    @Operation(summary = "말투 수정", description = "사용자가 생성한 말투를 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    public void updateMyMaltu(@AuthenticationPrincipal User user, @PathVariable Long maltuId,
                              @Valid @RequestBody UpdateMaltuRequestDto updateMaltuRequestDto) {
        maltuService.updateMyMaltu(user, maltuId, updateMaltuRequestDto);
    }

    @PutMapping("/{maltuId}")
    @Operation(summary = "말투 공개 설정 변경", description = "말투의 공개/비공개 설정을 변경합니다")
    @SecurityRequirement(name = "bearerAuth")
    public void updateShare(@AuthenticationPrincipal User user, @PathVariable Long maltuId,
                            @RequestParam("isPublic") boolean isPublic) {
        maltuService.updateShare(user, maltuId, isPublic);
    }

    @DeleteMapping("/{maltuId}")
    @Operation(summary = "말투 삭제", description = "사용자가 생성한 말투를 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    public void deleteMyMaltu(@AuthenticationPrincipal User user,
                              @PathVariable Long maltuId) {
        maltuService.deleteMyMaltu(user, maltuId);
    }
}
