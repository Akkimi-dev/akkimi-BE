package akkimi_BE.aja.repository;


import akkimi_BE.aja.entity.TodayDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

public interface TodayDateRepository extends JpaRepository<TodayDate, Long> {

    Optional<TodayDate> findByGoal_GoalIdAndTodayDate(Long goalId, LocalDate todayDate);

    List<TodayDate> findAllByGoal_GoalIdAndTodayDateBetween(Long goalId, LocalDate from, LocalDate to);
}
