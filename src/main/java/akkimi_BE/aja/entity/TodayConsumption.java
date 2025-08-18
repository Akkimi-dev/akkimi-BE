package akkimi_BE.aja.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "today_consumtion")
public class TodayConsumption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumption_id")
    private Long consumptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "today_date_id",nullable = false)
    private TodayDate todayDate;

    @Column(name = "category", length = 50,nullable = false)
    private String category;

    @Column(name = "item_name", length = 50, nullable = false)
    private String itemName;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "description",length = 255)
    private String description;

    public void change(String category, String itemName, Integer amount, String description) {
        this.category = category;
        this.itemName = itemName;
        this.amount = amount;
        this.description = description;
    }
}
