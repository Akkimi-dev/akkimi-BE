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

    //시스템에서 만들어둔 기본 말투인지
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    //공유 여부
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    //말투 프롬프트 텍스트
    @Column(columnDefinition = "LONGTEXT")
    private String prompt;

    public void updateMaltu(String maltuName, String prompt){
        this.maltuName = maltuName;
        this.prompt = prompt;
    }

    public void setMaltuNameAndPrompt(String maltuName, String prompt) {
        this.maltuName = maltuName;
        this.prompt = prompt;
    }
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}


