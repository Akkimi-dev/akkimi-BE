package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // Builder/Factory 전용
@Builder
@Table(name = "users")
public class User extends BaseTimeEntity implements UserDetails {
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_Type", nullable = false)
    private SocialType socialType;

    @Column(name = "social_id", length = 100, unique = true)
    private String socialId; // 소셜 전용, 로컬은 NULL

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 100)
    private String passwordHash; // 로컬 전용

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "region")
    private String region;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash != null ? passwordHash : "";
    }

    @Override
    public String getUsername() {
        return socialId;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeCurrentMaltu(Long maltuId) {
        this.currentMaltuId = maltuId;
    }

    public void updateRegion(String region) {
        this.region = region;
    }
}
