package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "chat_message")
public class ChatMessage extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long maltuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Speaker speaker;

    @Lob
    @Column(nullable = false)
    private String message;

    @Column(name = "is_feedback", nullable = false)
    private Boolean isFeedback;

    public static ChatMessage of(User user, Long maltuId, Speaker speaker, String message, Boolean isFeedback) {
        return ChatMessage.builder()
                .user(user)
                .maltuId(maltuId)
                .speaker(speaker)
                .message(message)
                .isFeedback(isFeedback)
                .build();
    }
}
