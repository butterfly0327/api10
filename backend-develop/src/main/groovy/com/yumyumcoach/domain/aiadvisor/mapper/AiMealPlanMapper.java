package com.yumyumcoach.domain.aiadvisor.mapper;

import com.yumyumcoach.domain.aiadvisor.entity.AiMealPlan;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiMealPlanMapper {
    AiMealPlan selectByEmailAndDate(@Param("email") String email, @Param("planDate") LocalDate planDate);

    void insert(@Param("plan") AiMealPlan plan);
}
