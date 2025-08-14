package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.request.CreateMaltuRequestDto;
import akkimi_BE.aja.dto.response.MaltuResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.MaltuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
