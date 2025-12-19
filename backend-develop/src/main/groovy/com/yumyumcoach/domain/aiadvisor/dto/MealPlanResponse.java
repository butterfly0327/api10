package com.yumyumcoach.domain.aiadvisor.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {
    private LocalDate planDate;
    private String breakfastMenu;
    private String lunchMenu;
    private String dinnerMenu;
    private Double breakfastCalories;
    private Double lunchCalories;
    private Double dinnerCalories;
    private String breakfastNote;
    private String lunchNote;
    private String dinnerNote;
    private Double totalCalories;
    private String modelUsed;
}
