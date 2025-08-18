package akkimi_BE.aja.controller;


import akkimi_BE.aja.dto.request.CreateTodayConsumptionRequestDto;
import akkimi_BE.aja.dto.request.UpdateTodayConsumptionDto;
import akkimi_BE.aja.entity.TodayConsumption;
import akkimi_BE.aja.entity.User;
import akkimi_BE.aja.service.TodayConsumptionService;
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
public class TodayConsumptionController {
    private final TodayConsumptionService todayConsumptionService;

    @PostMapping("/goals/{goalId}/days/{date}/consumptions")
    public Long createConsumption(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date,
            @RequestBody @Valid CreateTodayConsumptionRequestDto req
            ){
        return todayConsumptionService.create(user, goalId, date, req);
    }

    @PatchMapping("/consumptions/{consumptionId}")
    public void updateConsumption(
            @AuthenticationPrincipal User user,
            @PathVariable Long consumptionId,
            @RequestBody @Valid UpdateTodayConsumptionDto req
    ){
        todayConsumptionService.update(user, consumptionId, req);
    }

    @DeleteMapping("/consumptions/{consumptionId}")
    public void deleteConsumption(
            @AuthenticationPrincipal User user,
            @PathVariable Long consumptionId
    ){
        todayConsumptionService.delete(user, consumptionId);
    }

    @GetMapping("/goals/{goalId}/days")
    public List<TodayConsumption> getDay(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date
    ){
        return todayConsumptionService.getDay(user, goalId, date);
    }

    @GetMapping("/goals/{goalId}/days/summary")
    public Map<String,Object> getDaySummary(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date
    ){
        return todayConsumptionService.getDaySummary(user, goalId, date);
    }

    @GetMapping("/goals/{goalId}/month")
    public List<TodayConsumption> getmMonth(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth ym
    ){
        return todayConsumptionService.getMonth(user, goalId, ym);
    }

    @GetMapping("/goals/{goalId}/month/summary")
    public Map<String,Object> getMonthSummary(
            @AuthenticationPrincipal User user,
            @PathVariable Long goalId,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth ym
    ){
        return todayConsumptionService.getMonthSummary(user, goalId, ym);
    }

}
