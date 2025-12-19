package com.yumyumcoach.domain.aiadvisor.mapper;

import com.yumyumcoach.domain.aiadvisor.entity.AiExerciseEvaluation;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiExerciseEvaluationMapper {
    AiExerciseEvaluation selectByEmailAndReferenceDate(
            @Param("email") String email,
            @Param("referenceDate") LocalDate referenceDate
    );

    void insert(@Param("evaluation") AiExerciseEvaluation evaluation);
}
