package com.yumyumcoach.domain.ai.dto;

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
public class MealPlanResponse {
    private Long planId;
    private LocalDate date;
    private String dayOfWeek;
    private String model;
    private List<MealItem> meals;
    private String rawMessage;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealItem {
        private String mealType;
        private String menu;
        private Double calories;
        private String note;
    }
}
