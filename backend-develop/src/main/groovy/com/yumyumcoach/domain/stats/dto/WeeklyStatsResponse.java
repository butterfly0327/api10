package com.yumyumcoach.domain.stats.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyStatsResponse {

    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<DailyDietStat> dietStats;
    private List<DailyExerciseStat> exerciseStats;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyDietStat {
        private LocalDate date;
        private String dayOfWeek;
        private double carbs;
        private double protein;
        private double fat;
        private double calories;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyExerciseStat {
        private LocalDate date;
        private String dayOfWeek;
        private double durationMinutes;
        private double calories;
    }
}
