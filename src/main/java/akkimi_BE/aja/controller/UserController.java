package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.request.UpdateRegionRequest;
import akkimi_BE.aja.dto.response.CurrentMaltuResponseDto;
import akkimi_BE.aja.dto.response.UserProfileResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponseDto getUserProfile(@AuthenticationPrincipal User user) {
        return userService.getUserProfile(user);
    }

    //닉네임, 이메일, 전화번호, 업데이트는 따로 할지 고민

    @PutMapping("/me/region")
    public void updateRegion(@AuthenticationPrincipal User user,
                             @RequestBody UpdateRegionRequest request) {
        userService.updateRegion(user, request.getRegion());
    }

    @PutMapping("/me/current-maltu/{maltuId}")
    public void updateCurrentMaltu(@AuthenticationPrincipal User user,
                                   @PathVariable Long maltuId) {
        userService.updateCurrentMaltu(user, maltuId);
    }

    @GetMapping("/me/current-maltu")
    public CurrentMaltuResponseDto getCurrentMaltu(@AuthenticationPrincipal User user) {
        return userService.getCurrentMaltu(user);
    }
}
