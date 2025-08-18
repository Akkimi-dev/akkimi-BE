package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "today_date",
uniqueConstraints = {
        @UniqueConstraint(name = "uk_goal_date",columnNames = {"goal_id","today_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TodayDate extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "today_date_id")
    private Long todayDateId;

    @ManyToOne(fetch = FetchType.EAGER,optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private SavingGoal goal;

    @Column(name = "today_date",nullable = false,columnDefinition = "date")
    private LocalDate todayDate;

    @Column(name = "today_total_sum",nullable = false)
    private Integer todayTotalSum;

    @OneToMany(mappedBy = "todayDate",cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TodayConsumption> consumptions = new ArrayList<>();

   public void increaseTotal(int diff) {
       if (todayTotalSum == null) todayTotalSum = 0;
       this.todayTotalSum += diff;
       if (this.todayTotalSum < 0) this.todayTotalSum = 0;
   }

   public void addConsumption(TodayConsumption consumption) {
       consumptions.add(consumption);
   }

   public static TodayDate of(SavingGoal goal, LocalDate date) {
       return TodayDate.builder()
               .goal(goal)
               .todayDate(date)
               .build();
   }
    
}
