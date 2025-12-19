package com.yumyumcoach.domain.ai.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyExerciseReviewResponse {
    private Long reviewId;
    private LocalDate targetDate;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String assessment;
    private String recommendation;
    private String model;
    private String rawMessage;
}
