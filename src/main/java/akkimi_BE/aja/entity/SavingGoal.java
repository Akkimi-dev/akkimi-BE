package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // JPA용
@AllArgsConstructor(access = AccessLevel.PRIVATE)    // Builder/Factory 전용
@Builder
@Table(name = "saving_goal")
public class SavingGoal extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "purpose_budget", nullable=false)
    private Integer purposeBudget;

    @Column(name = "purpose", length = 50, nullable = false)
    private String purpose;

    @Column(name = "start_date", nullable=false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable=false)
    private LocalDate endDate;

    // 현재 목표 설정 여부
    @Column(name = "is_current_goal")
    private Boolean isCurrentGoal = true;

    @Column(name = "total_sum")
    private Integer totalSum = 0;

    // --- 계산 프로퍼티 (DB 미저장) ---
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 남은 일수(오늘이 0, 지났으면 음수) */
    @Transient
    public long getDDay() {
        LocalDate todayKst = LocalDate.now(KST);
        return ChronoUnit.DAYS.between(todayKst, endDate);
    }

    /** 표시용 라벨: D-n / D-Day / D+n */
    @Transient
    public String getDDayLabel() {
        long d = getDDay();
        if (d > 0) return "D-" + d;
        if (d == 0) return "D-Day";
        return "D+" + Math.abs(d);
    }

}
