package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.response.ChatHistoryResponseDto;
import akkimi_BE.aja.dto.response.ChatResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ChatResponseDto chat(@AuthenticationPrincipal User user, @RequestBody String message) {
        return chatService.talk(user, message);
    }

    @GetMapping
    public ChatHistoryResponseDto getMessages(@AuthenticationPrincipal User user,
                                              @RequestParam(name = "limit", required = false) Integer limit,
                                              @RequestParam(name = "beforeId", required = false) Long beforeId) {
        return chatService.getMessages(user, limit, beforeId);
    }
}
