package com.yumyumcoach.domain.exercise.service;

import com.yumyumcoach.domain.exercise.dto.*;
import com.yumyumcoach.domain.exercise.entity.Exercise;
import com.yumyumcoach.domain.exercise.entity.ExerciseRecord;
import com.yumyumcoach.domain.exercise.entity.ExerciseRecordWithExercise;
import com.yumyumcoach.domain.exercise.mapper.ExerciseMapper;
import com.yumyumcoach.domain.exercise.mapper.ExerciseRecordMapper;
import com.yumyumcoach.domain.user.entity.Profile;
import com.yumyumcoach.domain.user.mapper.ProfileMapper;
import com.yumyumcoach.global.exception.BusinessException;
import com.yumyumcoach.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {
    private final ExerciseMapper exerciseMapper;
    private final ExerciseRecordMapper exerciseRecordMapper;
    private final ProfileMapper profileMapper;

    public List<ExerciseResponse> getExercises() {
        return exerciseMapper.findAll().stream()
                .map(this::toExerciseResponse)
                .toList();
    }

    public List<ExerciseRecordResponse> getMyExerciseRecords(String email, java.time.LocalDate recordDate) {
        return exerciseRecordMapper.findByEmailAndDate(email, recordDate).stream()
                .map(this::toExerciseRecordResponse)
                .toList();
    }

    public ExerciseRecordResponse getMyExerciseRecordDetail(String email, Long recordId) {
        checkRecordOwnerOrThrow(email, recordId);

        ExerciseRecordWithExercise record = exerciseRecordMapper.findDetailByIdAndEmail(recordId, email);
        if (record == null) {
            throw new BusinessException(ErrorCode.EXERCISE_RECORD_NOT_FOUND);
        }
        return toExerciseRecordResponse(record);
    }

    @Transactional
    public List<ExerciseRecordResponse> createMyExerciseRecords(String email, List<ExerciseRecordRequest> requests) {
        return requests.stream()
                .map(req -> createExerciseRecord(email, req))
                .toList();
    }

    @Transactional
    public ExerciseRecordResponse updateMyExerciseRecord(String email, Long recordId, ExerciseRecordRequest request) {
        checkRecordOwnerOrThrow(email, recordId);

        double calories = calculateCalories(email, request.getExerciseId(), request.getDurationMinutes());

        ExerciseRecord exerciseRecord = ExerciseRecord.builder()
                .id(recordId)
                .email(email)
                .exerciseId(request.getExerciseId())
                .recordDate(request.getRecordDate())
                .durationMinutes(request.getDurationMinutes())
                .calories(calories)
                .build();

        exerciseRecordMapper.update(exerciseRecord);
        return getMyExerciseRecordDetail(email, recordId);
    }

    @Transactional
    public DeleteExerciseRecordResponse deleteMyExerciseRecord(String email, Long recordId) {
        checkRecordOwnerOrThrow(email, recordId);

        exerciseRecordMapper.delete(recordId, email);

        return DeleteExerciseRecordResponse.builder()
                .recordId(recordId)
                .deleted(true)
                .deletedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------
    // 비즈니스 예외(403/404)만
    // -------------------------
    private void checkRecordOwnerOrThrow(String email, Long recordId) {
        String ownerEmail = exerciseRecordMapper.findEmailByRecordId(recordId);

        if (ownerEmail == null) {
            throw new BusinessException(ErrorCode.EXERCISE_RECORD_NOT_FOUND);
        }
        if (!ownerEmail.equalsIgnoreCase(email)) {
            throw new BusinessException(ErrorCode.EXERCISE_RECORD_FORBIDDEN);
        }
    }

    private double calculateCalories(String email, Long exerciseId, Double durationMinutes) {
        Exercise exercise = exerciseMapper.findById(exerciseId);
        if (exercise == null) {
            throw new BusinessException(ErrorCode.EXERCISE_NOT_FOUND);
        }

        Profile profile = profileMapper.findByEmail(email);
        if (profile == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        }
        Double currentWeight = profile.getCurrentWeight();
        if (currentWeight == null) {
            // 몸무게가 지정이 안되어있으면 기본값인 60으로 계산
            currentWeight = 60.0;
        }

        double durationHours = durationMinutes / 60.0;
        double rawCalories = exercise.getMet() * currentWeight * durationHours;

        return BigDecimal.valueOf(rawCalories)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private ExerciseResponse toExerciseResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .exerciseId(exercise.getId())
                .name(exercise.getName())
                .met(exercise.getMet())
                .intensityLevel(exercise.getIntensityLevel())
                .type(exercise.getType())
                .description(exercise.getDescription())
                .build();
    }

    private ExerciseRecordResponse toExerciseRecordResponse(ExerciseRecordWithExercise record) {
        return ExerciseRecordResponse.builder()
                .recordId(record.getId())
                .exerciseId(record.getExerciseId())
                .exerciseName(record.getExerciseName())
                .met(record.getMet())
                .intensityLevel(record.getIntensityLevel())
                .type(record.getType())
                .recordDate(record.getRecordDate())
                .durationMinutes(record.getDurationMinutes())
                .calories(record.getCalories())
                .build();
    }

    private ExerciseRecordResponse createExerciseRecord(String email, ExerciseRecordRequest request) {
        double calories = calculateCalories(email, request.getExerciseId(), request.getDurationMinutes());

        ExerciseRecord exerciseRecord = ExerciseRecord.builder()
                .email(email)
                .exerciseId(request.getExerciseId())
                .recordDate(request.getRecordDate())
                .durationMinutes(request.getDurationMinutes())
                .calories(calories)
                .build();

        exerciseRecordMapper.insert(exerciseRecord);
        return getMyExerciseRecordDetail(email, exerciseRecord.getId());
    }
}

