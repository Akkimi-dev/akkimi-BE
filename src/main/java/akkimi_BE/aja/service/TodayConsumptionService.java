package akkimi_BE.aja.service;

import akkimi_BE.aja.dto.request.CreateTodayConsumptionRequestDto;
import akkimi_BE.aja.dto.request.UpdateTodayConsumptionDto;
import akkimi_BE.aja.entity.SavingGoal;
import akkimi_BE.aja.entity.TodayConsumption;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayConsumptionService {
    private  final SavingGoalRepository savingGoalRepository;
    private  final TodayConsumptionRepository todayConsumptionRepository;
    private  final TodayDateRepository todayDateRepository;
    private final ChatService chatService;

    @Transactional
    public Long create(User user, Long goalId, LocalDate date, CreateTodayConsumptionRequestDto createTodayConsumptionRequestDto) {
        SavingGoal goal = savingGoalRepository.findByGoalIdAndUser_UserId(goalId, user.getUserId())
                .orElseThrow(()-> new CustomException(HttpErrorCode.UNAUTHORIZED));

        validateDateInRange(date,goal);

        TodayDate todayDate = todayDateRepository
                .findByGoal_GoalIdAndTodayDate(goalId,date)
                .orElseGet(()->todayDateRepository.save(
                        TodayDate.builder().goal(goal).todayDate(date).todayTotalSum(0).build()
                ));

        TodayConsumption saved = todayConsumptionRepository.save(TodayConsumption.builder()
                .todayDate(todayDate)
                .category(createTodayConsumptionRequestDto.getCategory())
                .itemName(createTodayConsumptionRequestDto.getItemName())
                .amount(createTodayConsumptionRequestDto.getAmount())
                .description(createTodayConsumptionRequestDto.getDescription()).build());

        todayDate.increaseTotal(createTodayConsumptionRequestDto.getAmount());
        goal.increaseTotal(createTodayConsumptionRequestDto.getAmount());


        try {chatService.sendConsumptionFeedBack(user,saved);} catch (Exception ignore) {}

        return saved.getConsumptionId();

    }

    @Transactional
    public void update(User user, Long consumptionId, UpdateTodayConsumptionDto req) {
        TodayConsumption c = todayConsumptionRepository.findById(consumptionId)
                .orElseThrow(()-> new CustomException(HttpErrorCode.UNAUTHORIZED));

        SavingGoal goal = c.getTodayDate().getGoal();
        if (!goal.getUser().getUserId().equals(user.getUserId())) throw new CustomException(HttpErrorCode.UNAUTHORIZED);

        Integer before = c.getAmount();
        c.update(req.getCategory(), req.getItemName(), req.getAmount(), req.getDescription());
        Integer after = c.getAmount();
        int diff = (after == null ? 0 : after) - (before == null ? 0 : before);

        if (diff != 0) {
            c.getTodayDate().increaseTotal(diff);
            goal.increaseTotal(diff);
        }
    }

    @Transactional
    public void delete(User user,Long consumptionId) {
        TodayConsumption c = todayConsumptionRepository.findById(consumptionId)
                .orElseThrow(()-> new CustomException(HttpErrorCode.UNAUTHORIZED));

        SavingGoal goal = c.getTodayDate().getGoal();
        if (!goal.getUser().getUserId().equals(user.getUserId())) throw new CustomException(HttpErrorCode.UNAUTHORIZED);

        int amount = c.getAmount() == null ? 0 : c.getAmount();
        c.getTodayDate().increaseTotal(-amount);
        goal.increaseTotal(-amount);

        todayConsumptionRepository.delete(c);
    }

    public List<TodayConsumption> getDay(User user, Long goalId, LocalDate date) {
        SavingGoal goal = savingGoalRepository.findByGoalIdAndUser_UserId(goalId,user.getUserId()).orElseThrow(()->new CustomException(HttpErrorCode.UNAUTHORIZED));
        validateDateInRange(date,goal);

        TodayDate todayDate = todayDateRepository.findByGoal_GoalIdAndTodayDate(goalId,date)
                .orElse(null);

        if (todayDate == null) return List.of();
        return todayDate.getConsumptions();
    }

    public Map<String,Object> getDaySummary(User user, Long goalId, LocalDate date) {
        var list = getDay(user,goalId,date);
        int total = list.stream().mapToInt(c  -> c.getAmount() == null ? 0 : c.getAmount()).sum();

        var summary = list.stream().map(c -> Map.of("consumptionId",c.getConsumptionId(),"price",c.getAmount())).toList();

        return Map.of("date", date.toString(),"total",summary.size(),"summary",summary,"sum",total);
    }

    public List<TodayConsumption> getMonth(User user, Long goalId, YearMonth ym) {
        SavingGoal goal = savingGoalRepository.findByGoalIdAndUser_UserId(goalId,user.getUserId())
                .orElseThrow(()->new CustomException(HttpErrorCode.UNAUTHORIZED));

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        LocalDate from = start.isBefore(goal.getStartDate()) ? goal.getStartDate(): start;
        LocalDate to = end.isAfter(goal.getEndDate()) ? goal.getEndDate():end;
        if (from.isBefore(to)) return List.of();

        var dates = todayDateRepository.findAllByGoal_GoalIdAndTodayDateBetween(goalId,from,to);
        return dates.stream()
                .flatMap(d->d.getConsumptions().stream())
                .toList();
    }

    public Map<String,Object> getMonthSummary(User user, Long goalId, YearMonth ym) {
        var list = getMonth(user, goalId, ym);

        var byDate = list.stream().collect(Collectors.groupingBy(
                c->c.getTodayDate().getTodayDate(),
                Collectors.summingInt(c->c.getAmount() == null ? 0 : c.getAmount())));

        var summary = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> Map.of("date",e.getKey().toString(),"price",e.getValue())).toList();

        return Map.of("month", ym.toString(),"summary",summary);
    }

    private void validateDateInRange(LocalDate date, SavingGoal goal) {
        if(date.isBefore(goal.getStartDate()) || date.isAfter(goal.getEndDate())) {
            throw new CustomException(HttpErrorCode.UNAUTHORIZED);
        }
    }

}
