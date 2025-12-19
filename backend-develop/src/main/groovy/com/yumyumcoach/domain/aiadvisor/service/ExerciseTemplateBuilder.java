package com.yumyumcoach.domain.aiadvisor.service;

import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.user.dto.MyPageResponse;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class ExerciseTemplateBuilder {

    private ExerciseTemplateBuilder() {
    }

    public static String buildPrompt(MyPageResponse myPage, WeeklyStatsResponse stats, LocalDate referenceDate) {
        String exerciseSummary = stats.getExerciseStats().stream()
                .map(e -> String.format("%s(%s): 운동시간=%.1f분, 소모칼로리=%.1f",
                        e.getDate(), e.getDayOfWeekKorean(), e.getTotalDurationMinutes(), e.getTotalCalories()))
                .collect(Collectors.joining(" | "));

        String healthInfo = myPage == null || myPage.getHealth() == null ? "" : myPage.getHealth().toString();

        return "사용자의 건강 정보와 주간 운동 기록을 기반으로 운동량이 부족/적당/많음 중 어디에 해당하는지 평가하고,"
                + "다음에 해볼 만한 운동을 한국어로 제안해 주세요. JSON 결과를 반환하며 volumeStatus와 recommendation 필드를 포함합니다."
                + "referenceDate 이후 미래 날짜 데이터는 고려하지 마세요." +
                "referenceDate: " + referenceDate + "\n" +
                "건강 정보: " + healthInfo + "\n" +
                "주간 운동 요약: " + exerciseSummary;
    }
}
