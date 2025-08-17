package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.request.CreateMaltuRequestDto;
import akkimi_BE.aja.dto.request.UpdateMaltuRequestDto;
import akkimi_BE.aja.dto.response.MaltuResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.MaltuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maltus")
public class MaltuController {
    private final MaltuService maltuService;

    @PostMapping
    public Long createMaltu(@AuthenticationPrincipal User user, @RequestBody CreateMaltuRequestDto createMaltuRequestDto) {
        return maltuService.createMaltu(user, createMaltuRequestDto);
    }

    @GetMapping("/{maltuId}")
    public MaltuResponseDto getMaltu(@AuthenticationPrincipal User user, @PathVariable Long maltuId) {
        return maltuService.getMaltu(user, maltuId);
    }

    @GetMapping("/public/list")
    public List<MaltuResponseDto> getPublicMaltus(@AuthenticationPrincipal User user) {
        return maltuService.getPublicMaltus(user);
    }

    @GetMapping("/mine/list")
    public List<MaltuResponseDto> getMyMaltus(@AuthenticationPrincipal User user) {
        return maltuService.getMyMaltus(user);
    }

    @PatchMapping("/{maltuId}")
    public void updateMyMaltu(@AuthenticationPrincipal User user, @PathVariable Long maltuId,
                              @Valid @RequestBody UpdateMaltuRequestDto updateMaltuRequestDto) {
        maltuService.updateMyMaltu(user, maltuId, updateMaltuRequestDto);
    }

    @PutMapping("/{maltuId}")
    public void updateShare(@AuthenticationPrincipal User user, @PathVariable Long maltuId,
                            @RequestParam("isPublic") boolean isPublic) {
        maltuService.updateShare(user, maltuId, isPublic);
    }

    @DeleteMapping("/{maltuId}")
    public void deleteMyMaltu(@AuthenticationPrincipal User user,
                              @PathVariable Long maltuId) {
        maltuService.deleteMyMaltu(user, maltuId);
    }
}
