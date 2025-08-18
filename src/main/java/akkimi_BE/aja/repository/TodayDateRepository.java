package akkimi_BE.aja.repository;


import akkimi_BE.aja.entity.TodayDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import java.util.Optional;

public interface TodayDateRepository extends JpaRepository<TodayDate, Long> {

    Optional<TodayDate> findByDate(LocalDate date);
    boolean existsByDate(LocalDate date);
}
