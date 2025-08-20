package akkimi_BE.aja.controller;


import akkimi_BE.aja.dto.request.CreateTodayConsumptionRequestDto;
import akkimi_BE.aja.dto.request.UpdateTodayConsumptionDto;
import akkimi_BE.aja.dto.response.DayConsumptionSummaryDto;
import akkimi_BE.aja.dto.response.MonthConsumptionSummaryDto;
import akkimi_BE.aja.dto.response.TodayConsumptionResponseDto;
import akkimi_BE.aja.entity.TodayConsumption;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.TodayConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "일일 소비 API")
public class TodayConsumptionController {
    private final TodayConsumptionService todayConsumptionService;

    @Operation(summary = "소비 내역 생성", description = "오늘의 소비 내역을 등록합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/goals/{goalId}/days/{date}/consumptions")
    public Long createConsumption(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date,
            @RequestBody @Valid CreateTodayConsumptionRequestDto req
            ){
        return todayConsumptionService.create(user, goalId, date, req);
    }

    @Operation(summary = "소비 내역 수정", description = "기존 소비 내역을 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/consumptions/{consumptionId}")
    public void updateConsumption(
            @AuthenticationPrincipal User user,
            @PathVariable Long consumptionId,
            @RequestBody @Valid UpdateTodayConsumptionDto req
    ){
        todayConsumptionService.update(user, consumptionId, req);
    }

    @Operation(summary = "소비 내역 삭제", description = "소비 내역을 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/consumptions/{consumptionId}")
    public void deleteConsumption(
            @AuthenticationPrincipal User user,
            @PathVariable Long consumptionId
    ){
        todayConsumptionService.delete(user, consumptionId);
    }

    @Operation(summary = "일 소비 내역 조회", description = "특정 날짜의 모든 소비 내역을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals/{goalId}/days")
    public List<TodayConsumptionResponseDto> getDay(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date
    ){
        return todayConsumptionService.getDay(user, goalId, date);
    }

    @Operation(summary = "일 소비 요약 조회", description = "특정 날짜의 소비 요약 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals/{goalId}/days/summary")
    public DayConsumptionSummaryDto getDaySummary(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date
    ){
        return todayConsumptionService.getDaySummary(user, goalId, date);
    }

    @Operation(summary = "달 소비 내역 조회", description = "특정 월의 모든 소비 내역을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals/{goalId}/month")
    public List<TodayConsumptionResponseDto> getmMonth(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth ym
    ){
        return todayConsumptionService.getMonth(user, goalId, ym);
    }

    @Operation(summary = "달 소비 요약 조회", description = "특정 월의 소비 요약 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/goals/{goalId}/month/summary")
    public MonthConsumptionSummaryDto getMonthSummary(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth ym
    ){
        return todayConsumptionService.getMonthSummary(user, goalId, ym);
    }

}
