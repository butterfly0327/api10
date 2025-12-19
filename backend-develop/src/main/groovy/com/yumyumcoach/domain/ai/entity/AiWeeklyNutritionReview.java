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
public class AiWeeklyNutritionReview {
    private Long id;
    private String email;
    private LocalDate targetDate;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String model;
    private String nutritionAssessment;
    private String guidance;
    private String requestContext;
    private LocalDateTime createdAt;
}
