package akkimi_BE.aja.controller;


import akkimi_BE.aja.dto.request.CreateSavingGoalRequestDto;
import akkimi_BE.aja.dto.request.UpdateSavingGoalRequestDto;
import akkimi_BE.aja.dto.response.SavingGoalResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.SavingGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "절약 목표 API")
public class SavingGoalController {

    private final SavingGoalService savingGoalService;

    @Operation(summary = "현재 진행 중인 목표 조회", description = "사용자의 현재 진행 중인 절약 목표를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals/current")
    public SavingGoalResponseDto getCurrentGoal(@AuthenticationPrincipal User user) {
        return savingGoalService.getCurrentGoal(user);
    }

    @Operation(summary = "전체 목표 목록 조회", description = "사용자의 모든 절약 목표를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals")
    public List<SavingGoalResponseDto> getAllGoals(@AuthenticationPrincipal User user) {
        return savingGoalService.getAllGoals(user);
    }

    @Operation(summary = "특정 목표 조회", description = "목표 ID로 특정 절약 목표를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals/{goalId}")
    public SavingGoalResponseDto getGoalById(@AuthenticationPrincipal User user
            ,@PathVariable Long goalId) {
        return savingGoalService.getGoalById(user, goalId);
    }

    @Operation(summary = "새 절약 목표 생성", description = "새로운 절약 목표를 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/saving-goals")
    public Map<String,Long> createSavingGoal(@AuthenticationPrincipal User user, @Valid @RequestBody CreateSavingGoalRequestDto createSavingGoalRequestDto) {
        Long id = savingGoalService.createSavingGoal(user, createSavingGoalRequestDto);
        return Map.of("goalId", id);
    }

    @Operation(summary = "목표 수정", description = "기존 절약 목표를 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/goals/{goalId}")
    public void updateGoal(@AuthenticationPrincipal User user,@PathVariable Long goalId,@Valid @RequestBody UpdateSavingGoalRequestDto updateSavingGoalRequestDto) {
        savingGoalService.updateGoal(user, goalId, updateSavingGoalRequestDto);
    }

    @Operation(summary = "목표 삭제", description = "절약 목표를 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/goals/{goalId}")
    public void deleteGoal(@AuthenticationPrincipal User user,@PathVariable Long goalId) {
        savingGoalService.deleteGoal(user, goalId);
    }
}

