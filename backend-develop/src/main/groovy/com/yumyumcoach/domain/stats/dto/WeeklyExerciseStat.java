package com.yumyumcoach.domain.stats.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyExerciseStat {
    private LocalDate date;
    private String dayOfWeekKorean;
    private double totalDurationMinutes;
    private double totalCalories;
}
