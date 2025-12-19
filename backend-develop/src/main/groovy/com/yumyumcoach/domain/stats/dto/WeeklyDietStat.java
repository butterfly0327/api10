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
public class WeeklyDietStat {
    private LocalDate date;
    private String dayOfWeekKorean;
    private double totalCalories;
    private double totalCarbs;
    private double totalProtein;
    private double totalFat;
}
