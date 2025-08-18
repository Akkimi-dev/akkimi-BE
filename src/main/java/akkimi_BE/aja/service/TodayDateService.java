package akkimi_BE.aja.service;

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
    public TodayDate ensureByDate(LocalDate date) {
        return todayDateRepository.findByDate(date)
                .orElseGet(()-> todayDateRepository.save(TodayDate.of(date)));
    }
}
