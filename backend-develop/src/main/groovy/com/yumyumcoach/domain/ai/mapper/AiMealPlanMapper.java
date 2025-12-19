package com.yumyumcoach.domain.ai.mapper;

import com.yumyumcoach.domain.ai.entity.AiDailyMealPlan;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiMealPlanMapper {
    AiDailyMealPlan findByEmailAndDate(
            @Param("email") String email,
            @Param("planDate") LocalDate planDate
    );

    void upsertMealPlan(AiDailyMealPlan mealPlan);
}
