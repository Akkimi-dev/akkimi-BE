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

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String message;

    @Column(name = "is_feedback", nullable = false)
    private Boolean isFeedback;

    @Column(name = "consumption_id")
    private Long consumptionId; //소비내역 생성 때 해당되는 유저, 봇 메시지에 저장

    public static ChatMessage of(User user, Long maltuId, Speaker speaker, String message, Boolean isFeedback, Long consumptionId) {
        return ChatMessage.builder()
                .user(user)
                .maltuId(maltuId)
                .speaker(speaker)
                .message(message)
                .isFeedback(isFeedback)
                .consumptionId(consumptionId)
                .build();
    }
}
