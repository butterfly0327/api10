package com.yumyumcoach.domain.exercise.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 운동 기록 + 운동 기본 정보 조인 결과.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRecordWithExercise {
    private Long id;
    private Long exerciseId;
    private String exerciseName;
    private Double met;
    private String intensityLevel;
    private String type;
    private LocalDate recordDate;
    private Double durationMinutes;
    private Double calories;
}
