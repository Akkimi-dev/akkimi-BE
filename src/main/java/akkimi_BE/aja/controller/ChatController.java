package akkimi_BE.aja.controller;

import akkimi_BE.aja.dto.request.ChatRequestDto;
import akkimi_BE.aja.dto.response.ChatHistoryResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "채팅 API")
public class ChatController {
    private final ChatService chatService;

    //SSE 스트리밍 응답
    /** 1) 사용자 메시지 전송(저장) → messageId 반환 */
    @PostMapping("/messages")
    @Operation(summary = "메시지 저장", description = "사용자의 채팅 메시지를 저장하고 messageId를 반환합니다")
    @SecurityRequirement(name = "bearerAuth")
    public Long sendMessage(@AuthenticationPrincipal User user, @RequestBody ChatRequestDto chatRequestDto) {
        return chatService.saveMessage(user, chatRequestDto);
    }

    /** 2) 해당 messageId에 대한 답변을 SSE로 스트리밍 */
    @GetMapping(value = "/sse/messages/{messageId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "스트림 대화 답변 받기", description = "메시지에 대한 챗봇의 답변을 SSE로 스트리밍합니다")
    @SecurityRequirement(name = "bearerAuth")
    public SseEmitter streamMessage(@AuthenticationPrincipal User user, @PathVariable Long messageId) {
        return chatService.streamReply(user, messageId);
    }

    @GetMapping("/history")
    @Operation(summary = "최근 대화 조회", description = "사용자의 채팅 대화 기록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public ChatHistoryResponseDto getMessages(@AuthenticationPrincipal User user,
                                              @RequestParam(name = "limit", required = false) Integer limit,
                                              @RequestParam(name = "beforeId", required = false) Long beforeId) {
        return chatService.getMessages(user, limit, beforeId);
    }
}
