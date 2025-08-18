package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.request.ChatRequestDto;
import akkimi_BE.aja.dto.response.ChatHistoryResponseDto;
import akkimi_BE.aja.dto.response.ChatResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    //SSE 스트리밍 응답
    /** 1) 사용자 메시지 전송(저장) → messageId 반환 */
    @PostMapping("/messages")
    public Long sendMessage(@AuthenticationPrincipal User user, @RequestBody ChatRequestDto chatRequestDto) {
        return chatService.saveMessage(user, chatRequestDto);
    }

    /** 2) 해당 messageId에 대한 답변을 SSE로 스트리밍 */
    @GetMapping(value = "/messages/{messageId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(@AuthenticationPrincipal User user, @PathVariable Long messageId) {
        return chatService.streamReply(user, messageId);
    }

    @GetMapping("/history")
    public ChatHistoryResponseDto getMessages(@AuthenticationPrincipal User user,
                                              @RequestParam(name = "limit", required = false) Integer limit,
                                              @RequestParam(name = "beforeId", required = false) Long beforeId) {
        return chatService.getMessages(user, limit, beforeId);
    }
}
