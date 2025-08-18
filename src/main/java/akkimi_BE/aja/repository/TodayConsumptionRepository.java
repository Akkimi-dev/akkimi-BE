package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.TodayConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TodayConsumptionRepository extends JpaRepository<TodayConsumption, Long> {

}
