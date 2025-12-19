package com.yumyumcoach.domain.aiadvisor.mapper;

import com.yumyumcoach.domain.aiadvisor.entity.AiNutritionEvaluation;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiNutritionEvaluationMapper {
    AiNutritionEvaluation selectByEmailAndReferenceDate(
            @Param("email") String email,
            @Param("referenceDate") LocalDate referenceDate
    );

    void insert(@Param("evaluation") AiNutritionEvaluation evaluation);
}
