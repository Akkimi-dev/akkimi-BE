package akkimi_BE.aja.service;

import akkimi_BE.aja.entity.TodayConsumption;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

/*
 일일 소비 생성 후
 챗봇에게 보낼 메시지로 변환하는 프롬프트
 */
@Component
public class FeedbackPromptBuilder {
    private static final NumberFormat money = NumberFormat.getInstance(Locale.KOREA);

    public String buildUserUtterance(TodayConsumption consumption) {
        StringBuilder sb = new StringBuilder();

        sb.append("오늘")
                .append(consumption.getCategory()).append("로 ")
                .append(consumption.getItemName()).append("에 ")
                .append(consumption.getAmount()).append("원 썼어.");

        if (consumption.getDescription() != null && !consumption.getDescription().isEmpty()) {
            sb.append("변명:").append(consumption.getDescription());
        }

        sb.append("\"위 소비 내역을 바탕으로, 오늘의 소비 습관에 대한 코칭 피드백을 간결하게 알려주세요.\"");
        return sb.toString();
    }
    //TODO 정후 : 일일소비생성 dto로 받아와 프롬프트 수정하기. 아래는 예시
//    public String buildUserMessage(DailyConsumptionCreateRequestDto req) {
//        var nf = NumberFormat.getInstance(Locale.KOREA);
//
//        int total = req.getItems() == null ? 0 :
//                req.getItems().stream().mapToInt(i -> i.getPrice() == null ? 0 : i.getPrice()).sum();
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("다음은 오늘의 소비 기록입니다.\n");
//        sb.append("- 날짜: ").append(req.getDate()).append("\n");
//
//        if (req.getItems() != null && !req.getItems().isEmpty()) {
//            sb.append("- 항목:\n");
//            req.getItems().forEach(i -> sb.append("  • ")
//                    .append(i.getName() == null ? "이름없음" : i.getName())
//                    .append(" / ")
//                    .append(i.getPrice() == null ? "0" : nf.format(i.getPrice())).append("원")
//                    .append(i.getCategory() == null ? "" : " / " + i.getCategory())
//                    .append("\n"));
//        } else {
//            sb.append("- 항목: (없음)\n");
//        }
//
//        sb.append("- 메모: ").append(req.getMemo() == null ? "(없음)" : req.getMemo()).append("\n");
//        sb.append("- 총액 추정: ").append(nf.format(total)).append("원\n\n");
//
//        sb.append("위 소비 내역을 바탕으로, 오늘의 소비 습관에 대한 코칭 피드백을 간결하게 알려주세요.");
//        return sb.toString();
//    }
}
