package akkimi_BE.aja.controller;


import akkimi_BE.aja.dto.request.CreateSavingGoalRequestDto;
import akkimi_BE.aja.dto.request.UpdateSavingGoalRequestDto;
import akkimi_BE.aja.dto.response.SavingGoalResponseDto;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.global.response.CommonResponse;
import akkimi_BE.aja.service.SavingGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "절약 목표 API")
public class SavingGoalController {

    private final SavingGoalService savingGoalService;

    @Operation(summary = "현재 진행 중인 목표 조회", description = "사용자의 현재 진행 중인 절약 목표를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals/current")
    public Optional<SavingGoalResponseDto> getCurrentGoal(@AuthenticationPrincipal User user) {
        SavingGoalResponseDto dto = savingGoalService.getCurrentGoal(user);
        return Optional.ofNullable(dto);
    }

    @Operation(summary = "전체 목표 목록 조회", description = "사용자의 모든 절약 목표를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals")
    public CommonResponse<List<SavingGoalResponseDto>> getAllGoals(@AuthenticationPrincipal User user) {
        List<SavingGoalResponseDto> dtos = savingGoalService.getAllGoals(user);
        return CommonResponse.of(200,"요청이 성공적으로 처리되었습니다.",dtos);
    }

    @Operation(summary = "특정 목표 조회", description = "목표 ID로 특정 절약 목표를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals/{goalId}")
    public CommonResponse<SavingGoalResponseDto> getGoalById(@AuthenticationPrincipal User user
            ,@PathVariable Long goalId) {
        SavingGoalResponseDto dto = savingGoalService.getGoalById(user, goalId);
        return CommonResponse.of(200,"요청이 성공적으로 처리되었습니다.",dto);
    }

    @Operation(summary = "새 절약 목표 생성", description = "새로운 절약 목표를 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/saving-goals")
    public CommonResponse<Map<String,Long>> createSavingGoal(@AuthenticationPrincipal User user, @Valid @RequestBody CreateSavingGoalRequestDto createSavingGoalRequestDto) {
        Long id = savingGoalService.createSavingGoal(user, createSavingGoalRequestDto);
        return CommonResponse.of(201,"생성이 성공적으로 처리되었습니다.",Map.of("goalId", id));
    }

    @Operation(summary = "목표 수정", description = "기존 절약 목표를 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/goals/{goalId}")
    public CommonResponse<Void> updateGoal(@AuthenticationPrincipal User user,@PathVariable Long goalId,@Valid @RequestBody UpdateSavingGoalRequestDto updateSavingGoalRequestDto) {
        savingGoalService.updateGoal(user, goalId, updateSavingGoalRequestDto);
        return CommonResponse.success(200,"요청이 성공적으로 처리되었습니다.");
    }

    @Operation(summary = "목표 삭제", description = "절약 목표를 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/goals/{goalId}")
    public CommonResponse<Void> deleteGoal(@AuthenticationPrincipal User user,@PathVariable Long goalId) {
        savingGoalService.deleteGoal(user, goalId);
        return CommonResponse.success(200,"요청이 성공적으로 처리되었습니다.");

    }
}

