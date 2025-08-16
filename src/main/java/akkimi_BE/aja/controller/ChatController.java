package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.response.ChatHistoryResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/{userId}")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public void chat(@AuthenticationPrincipal User user, @RequestBody String message) {
        chatService.talk(user, message);
    }

    @GetMapping("/messages")
    public ChatHistoryResponseDto getMessages(@AuthenticationPrincipal User user,
                                              @RequestParam(name = "limit", required = false) Integer limit,
                                              @RequestParam(name = "beforeId", required = false) Long beforeId) {
        return chatService.getMessages(user, limit, beforeId);
    }
}
