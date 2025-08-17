package akkimi_BE.aja.dto.response;


import akkimi_BE.aja.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatResponseDto {
    private MessageDto userMessage;
    private MessageDto botMessage;

    @Getter @Builder
    public static class MessageDto {
        private Long messageId;
        private String role;      // "USER" | "BOT"
        private String text;
        private String createdAt; // ISO-8601
    }

    public static ChatResponseDto of(ChatMessage user, ChatMessage bot) {
        return ChatResponseDto.builder()
                .userMessage(toDto(user))
                .botMessage(toDto(bot))
                .build();
    }
    private static MessageDto toDto(ChatMessage m) {
        return MessageDto.builder()
                .messageId(m.getChatId())
                .role(m.getSpeaker().name())
                .text(m.getMessage())
                .createdAt(m.getCreatedAt().toString())
                .build();
    }
}
