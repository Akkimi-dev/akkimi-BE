package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "refresh_token")
public class RefreshToken extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshTokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 실제 리프레시 토큰 문자열 (서명된 JWT 등) */
    @Column(name = "token", nullable = false, length = 512)
    private String token;

    /** 만료 시각(서버 관점) */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public boolean isExpired(LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }
}
