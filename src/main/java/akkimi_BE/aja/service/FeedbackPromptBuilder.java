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

    public String buildUserUtterance(TodayConsumption c) {
        String category = safe(c.getCategory());
        String itemName = safe(c.getItemName());
        String desc = safe(c.getDescription());
        String amountStr = (c.getAmount() == null) ? "-" : money.format(c.getAmount()) + "원";

        StringBuilder sb = new StringBuilder();
        sb.append("[오늘 소비 기록]\n")
                .append("카테고리: ").append(category.isEmpty() ? "-" : category).append("\n")
                .append("이름: ").append(itemName.isEmpty() ? "-" : itemName).append("\n")
                .append("금액: ").append(amountStr).append("\n")
                .append("사담: ").append(desc.isEmpty() ? "-" : desc).append("\n\n")
                .append("위 소비 내역을 바탕으로, 오늘의 소비 습관에 대한 코칭 피드백을 간결하게 알려주세요.");

        return sb.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}