package akkimi_BE.aja.repository;

import akkimi_BE.aja.entity.SavingGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {


    Optional<SavingGoal> findByUser_UserIdAndIsCurrentGoalTrue(Long userId);

    List<SavingGoal> findAllByUser_UserId(Long userId);

    boolean existsByUser_UserIdAndIsCurrentGoalTrue(Long userId);

    Optional<SavingGoal> findByGoalIdAndUser_UserId(Long goalId, Long userId);

}
