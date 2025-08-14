package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // Builder/Factory 전용
@Builder
@Table(name = "characters")
public class Character extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long characterId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maltu_id", nullable = false, unique = true)
    private Maltu maltu;

    @Column(name = "character_name", nullable = false, length = 50)
    private String characterName;
}
