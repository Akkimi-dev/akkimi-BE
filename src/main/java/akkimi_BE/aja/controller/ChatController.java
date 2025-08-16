package akkimi_BE.aja.controller;

import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.UserRepository;
import akkimi_BE.aja.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/{userId}")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping
    public String chat(@RequestBody String message, @PathVariable Long userId) {//@AuthenticationPrincipal User user
        User user = userRepository.findById(userId).orElse(null);
        return chatService.talk(user, message);
    }
}
