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
public class NutritionEvaluationResponse {
    private LocalDate referenceDate;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String carbohydrateStatus;
    private String proteinStatus;
    private String fatStatus;
    private String calorieStatus;
    private String analysisSummary;
    private String modelUsed;
}
