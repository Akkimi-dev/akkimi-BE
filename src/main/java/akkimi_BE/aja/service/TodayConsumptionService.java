package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.CreateTodayConsumptionRequestDto;
import akkimi_BE.aja.entity.SavingGoal;
import akkimi_BE.aja.entity.TodayDate;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.global.exception.CustomException;
import akkimi_BE.aja.global.exception.HttpErrorCode;
import akkimi_BE.aja.repository.SavingGoalRepository;
import akkimi_BE.aja.repository.TodayConsumptionRepository;
import akkimi_BE.aja.repository.TodayDateRepository;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayConsumptionService {
    private  final SavingGoalRepository savingGoalRepository;
    private  final TodayConsumptionRepository todayConsumptionRepository;
    private  final TodayDateRepository todayDateRepository;

    @Transactional
    public Long create(User user, Long goalId, LocalDate date, CreateTodayConsumptionRequestDto createTodayConsumptionRequestDto) {
        SavingGoal goal = savingGoalRepository.findByGoalIdAndUser_UserId(goalId, user.getUserId())
                .orElseThrow(()-> new CustomException(HttpErrorCode.UNAUTHORIZED));


    }
}
