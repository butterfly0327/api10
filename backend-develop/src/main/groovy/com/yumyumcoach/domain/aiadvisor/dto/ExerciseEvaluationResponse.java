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
public class ExerciseEvaluationResponse {
    private LocalDate referenceDate;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String volumeStatus;
    private String recommendation;
    private String modelUsed;
}
