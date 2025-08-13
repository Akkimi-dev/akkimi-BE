package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // Builder/Factory 전용
@Builder
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** 현재 선택된 말투 FK 값만 보관 (연관관계는 Maltu에서 역방향으로 접근) */
    @Column(name = "current_maltu_id")
    private Long currentMaltuId;

    //소비 캐릭터
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @Column(name = "kakao_id")
    private String kakaoId;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "region")
    private String region;
}
