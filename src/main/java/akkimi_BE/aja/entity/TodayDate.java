package akkimi_BE.aja.entity;

import akkimi_BE.aja.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "today_date",
uniqueConstraints = {
        @UniqueConstraint(name = "uk_today_user-date",columnNames = {"user_id","date"})
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


    @Column(name = "date",nullable = false,columnDefinition = "date")
    private LocalDate date;

    public static TodayDate of(LocalDate date) {
        return TodayDate.builder()
                .date(date).build();
    }
    
}
