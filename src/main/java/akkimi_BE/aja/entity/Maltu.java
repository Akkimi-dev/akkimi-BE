package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // Builder/Factory 전용
@Builder
@Table(name = "maltu")
public class Maltu extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maltu_id")
    private Long maltuId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false) //소유자
    private User creator;

    @Column(name = "maltu_name", nullable = false, length = 50)
    private String maltuName;

    //공유 여부
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    //말투 프롬프트 텍스트
    @Lob
    private String prompt;

    public void updateMaltu(String maltuName, String prompt){
        this.maltuName = maltuName;
        this.prompt = prompt;
    }
}


