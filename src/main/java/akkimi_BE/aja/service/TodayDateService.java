package akkimi_BE.aja.service;

import akkimi_BE.aja.entity.SavingGoal;
import akkimi_BE.aja.entity.TodayDate;
import akkimi_BE.aja.repository.TodayDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayDateService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TodayDateRepository todayDateRepository;

    public LocalDate todayKst(){
        return LocalDate.now(KST);
    }

    @Transactional
    public TodayDate ensureByGoalAndDate(SavingGoal goal, LocalDate date) {
        return todayDateRepository.findByGoal_GoalIdAndTodayDate(goal.getGoalId(),date)
                .orElseGet(()-> todayDateRepository.save(TodayDate.of(goal,date)));
    }
}
