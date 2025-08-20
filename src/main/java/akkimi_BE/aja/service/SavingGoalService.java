package akkimi_BE.aja.service;


import akkimi_BE.aja.dto.request.CreateSavingGoalRequestDto;
import akkimi_BE.aja.dto.request.UpdateSavingGoalRequestDto;
import akkimi_BE.aja.dto.response.SavingGoalResponseDto;
import akkimi_BE.aja.entity.SavingGoal;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.repository.SavingGoalRepository;
import akkimi_BE.aja.repository.UserRepository;
import akkimi_BE.aja.global.exception.CustomException;
import akkimi_BE.aja.global.exception.HttpErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavingGoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final UserRepository userRepository;

    public SavingGoalResponseDto getCurrentGoal(User user) {
        SavingGoal goal = savingGoalRepository
                .findByUser_UserIdAndIsCurrentGoalTrue(user.getUserId())
                .orElseThrow(()-> new CustomException(HttpErrorCode.NOT_FOUND));
        return  SavingGoalResponseDto.from(goal);

    }

    public List<SavingGoalResponseDto> getAllGoals(User user) {
        return savingGoalRepository.findAllByUser_UserId(user.getUserId()).stream()
                .map(SavingGoalResponseDto::from)
                .toList();

    }

    public SavingGoalResponseDto getGoalById(User user, Long goalId) {
        SavingGoal goal = savingGoalRepository
                .findByGoalIdAndUser_UserId(goalId, user.getUserId())
                .orElseThrow(()-> new CustomException(HttpErrorCode.NOT_FOUND));
        return SavingGoalResponseDto.from(goal);
    }

    @Transactional
    public  Long createSavingGoal(User user, CreateSavingGoalRequestDto requestDto) {
        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new CustomException(HttpErrorCode.BAD_REQUST);
        }
        if (savingGoalRepository.existsByUser_UserIdAndIsCurrentGoalTrue(user.getUserId())) {
            throw new CustomException(HttpErrorCode.BAD_REQUST);
        }

        SavingGoal saved = savingGoalRepository.save(
                SavingGoal.builder()
                        .user(user)
                        .purpose(requestDto.getPurpose())
                        .purposeBudget(requestDto.getPurposeBudget())
                        .startDate(requestDto.getStartDate())
                        .endDate(requestDto.getEndDate())
                        .isCurrentGoal(true)
                        .totalSum(0)
                .build()
        );
        return saved.getGoalId();
    }

    @Transactional
    public void updateGoal(User user, Long goalId, UpdateSavingGoalRequestDto requestDto) {
        SavingGoal goal = savingGoalRepository
                .findByGoalIdAndUser_UserId(goalId, user.getUserId())
                .orElseThrow(()-> new CustomException(HttpErrorCode.BAD_REQUST));
        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new CustomException(HttpErrorCode.BAD_REQUST);
        }
        goal.updatePartial(requestDto.getPurposeBudget(),requestDto.getPurpose(),requestDto.getStartDate(),requestDto.getEndDate());
    }

    @Transactional
    public void deleteGoal(User user, Long goalId) {
        SavingGoal goal = savingGoalRepository
                .findByGoalIdAndUser_UserId(goalId, user.getUserId())
                .orElseThrow(()->new CustomException(HttpErrorCode.NOT_FOUND));
    }
}
