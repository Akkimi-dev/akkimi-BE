package akkimi_BE.aja.controller;


import akkimi_BE.aja.dto.request.CreateSavingGoalRequestDto;
import akkimi_BE.aja.dto.request.UpdateSavingGoalRequestDto;
import akkimi_BE.aja.dto.response.SavingGoalResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.SavingGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SavingGoalController {

    private final SavingGoalService savingGoalService;

    @GetMapping("/goals/current")
    public SavingGoalResponseDto getCurrentGoal(@AuthenticationPrincipal User user) {
        return savingGoalService.getCurrentGoal(user);
    }

    @GetMapping("/goals")
    public List<SavingGoalResponseDto> getAllGoals(@AuthenticationPrincipal User user) {
        return savingGoalService.getAllGoals(user);
    }

    @GetMapping("/goals/{goalId}")
    public SavingGoalResponseDto getGoalById(@AuthenticationPrincipal User user
            ,@PathVariable Long goalId) {
        return savingGoalService.getGoalById(user, goalId);
    }

    @PostMapping("/saving-goals")
    public Map<String,Long> createSavingGoal(@AuthenticationPrincipal User user, @Valid @RequestBody CreateSavingGoalRequestDto createSavingGoalRequestDto) {
        Long id = savingGoalService.createSavingGoal(user, createSavingGoalRequestDto);
        return Map.of("goalId", id);
    }

    @PatchMapping("/goals/{goalId}")
    public void updateGoal(@AuthenticationPrincipal User user,@PathVariable Long goalId,@Valid @RequestBody UpdateSavingGoalRequestDto updateSavingGoalRequestDto) {
        savingGoalService.updateGoal(user, goalId, updateSavingGoalRequestDto);
    }

    @DeleteMapping("/goals/{goalId}")
    public void deleteGoal(@AuthenticationPrincipal User user,@PathVariable Long goalId) {
        savingGoalService.deleteGoal(user, goalId);
    }
}

