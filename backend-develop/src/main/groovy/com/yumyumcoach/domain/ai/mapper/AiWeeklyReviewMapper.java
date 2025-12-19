package com.yumyumcoach.domain.ai.mapper;

import com.yumyumcoach.domain.ai.entity.AiWeeklyExerciseReview;
import com.yumyumcoach.domain.ai.entity.AiWeeklyNutritionReview;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiWeeklyReviewMapper {

    AiWeeklyNutritionReview findNutritionByEmailAndDate(
            @Param("email") String email,
            @Param("targetDate") LocalDate targetDate
    );

    void upsertNutrition(AiWeeklyNutritionReview review);

    AiWeeklyExerciseReview findExerciseByEmailAndDate(
            @Param("email") String email,
            @Param("targetDate") LocalDate targetDate
    );

    void upsertExercise(AiWeeklyExerciseReview review);
}
