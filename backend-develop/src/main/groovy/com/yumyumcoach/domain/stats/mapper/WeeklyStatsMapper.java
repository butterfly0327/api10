package com.yumyumcoach.domain.stats.mapper;

import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeeklyStatsMapper {

    List<WeeklyStatsResponse.DailyDietStat> aggregateDietByDate(
            @Param("email") String email,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<WeeklyStatsResponse.DailyExerciseStat> aggregateExerciseByDate(
            @Param("email") String email,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
