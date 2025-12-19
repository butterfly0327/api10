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
public class AiNutritionEvaluation {
    private Long id;
    private String email;
    private LocalDate referenceDate;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String carbohydrateStatus;
    private String proteinStatus;
    private String fatStatus;
    private String calorieStatus;
    private String analysisSummary;
    private String modelUsed;
    private LocalDateTime createdAt;
}
