package com.yumyumcoach.domain.ai.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDailyMealPlan {
    private Long id;
    private String email;
    private LocalDate planDate;
    private String model;
    private String breakfastMenu;
    private Double breakfastCalories;
    private String breakfastSummary;
    private String lunchMenu;
    private Double lunchCalories;
    private String lunchSummary;
    private String dinnerMenu;
    private Double dinnerCalories;
    private String dinnerSummary;
    private String rawResponse;
    private String requestContext;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
