package com.yumyumcoach.domain.aiadvisor.entity;

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
public class AiMealPlan {
    private Long id;
    private String email;
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
    private LocalDateTime createdAt;
}
