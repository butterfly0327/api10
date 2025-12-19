package com.yumyumcoach.domain.aiadvisor.service;

import com.yumyumcoach.domain.stats.dto.WeeklyDietStat;
import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.user.dto.MyPageResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class NutritionTemplateBuilder {

    private NutritionTemplateBuilder() {
    }

    public static String buildPrompt(
            MyPageResponse myPage,
            WeeklyStatsResponse stats,
            LocalDate referenceDate,
            LocalDateTime now
    ) {
        String dietSummary = stats.getDietStats().stream()
                .map(d -> String.format("%s(%s): kcal=%.1f, 탄수=%.1f, 단백=%.1f, 지방=%.1f",
                        d.getDate(), d.getDayOfWeekKorean(), d.getTotalCalories(), d.getTotalCarbs(), d.getTotalProtein(), d.getTotalFat()))
                .collect(Collectors.joining(" | "));

        String healthInfo = myPage == null || myPage.getHealth() == null ? "" : myPage.getHealth().toString();

        return "주어진 사용자 건강 정보와 이번 주 식단 기록을 바탕으로 탄수화물/단백질/지방/칼로리 섭취 상태를 부족/적당/과다 중 하나로 평가하고,"
                + "한국어로 JSON 형태의 결과를 생성해 주세요. 키는 carbohydrateStatus, proteinStatus, fatStatus, calorieStatus, analysisSummary 입니다. "
                + "referenceDate 이후의 미래 날짜 데이터는 고려하지 말고, referenceDate 까지만 반영하세요." +
                "referenceDate: " + referenceDate + "\n" +
                "현재 시간: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                "건강 정보: " + healthInfo + "\n" +
                "주간 식단 요약: " + dietSummary;
    }
}
