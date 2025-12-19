package com.yumyumcoach.domain.aiadvisor.service;

import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.user.dto.MyPageResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class MealPlanTemplateBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private MealPlanTemplateBuilder() {
    }

    public static String buildPrompt(MyPageResponse myPage, WeeklyStatsResponse stats, LocalDate targetDate) {
        String healthInfo = myPage == null || myPage.getHealth() == null ? "" : String.format(
                "나이/생년월일: %s, 키: %s, 현재 체중: %s, 목표 체중: %s, 질환: 당뇨=%s, 고혈압=%s, 고지혈증=%s, 기타 질환=%s, 목표: %s, 활동 수준: %s",
                myPage.getHealth().getBirthDate(),
                myPage.getHealth().getHeight(),
                myPage.getHealth().getWeight(),
                myPage.getHealth().getGoalWeight(),
                myPage.getHealth().getHasDiabetes(),
                myPage.getHealth().getHasHypertension(),
                myPage.getHealth().getHasHyperlipidemia(),
                myPage.getHealth().getOtherDisease(),
                myPage.getHealth().getGoal(),
                myPage.getHealth().getActivityLevel()
        );

        String dietSummary = stats.getDietStats().stream()
                .map(d -> String.format("%s(%s): kcal=%.1f, 탄수=%.1f, 단백=%.1f, 지방=%.1f",
                        d.getDate(), d.getDayOfWeekKorean(), d.getTotalCalories(), d.getTotalCarbs(), d.getTotalProtein(), d.getTotalFat()))
                .collect(Collectors.joining(" | "));

        return "아래 정보를 참고하여 한국어로 JSON 형식의 하루 식단 계획을 만들어주세요. " +
                "키는 breakfast, lunch, dinner 이며 각 항목에 menu(문자열), calories(숫자), note(간단한 한줄평)를 포함해 주세요. " +
                "전체 칼로리를 계산하기 쉬운 값으로 주세요." +
                "사용자 건강 정보: " + healthInfo + "\n" +
                "주간 식단 요약: " + dietSummary + "\n" +
                "요청 날짜: " + DATE_FORMATTER.format(targetDate);
    }
}
