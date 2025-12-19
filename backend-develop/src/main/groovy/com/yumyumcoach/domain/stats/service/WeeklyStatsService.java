package com.yumyumcoach.domain.stats.service;

import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.stats.mapper.WeeklyStatsMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeeklyStatsService {

    private final WeeklyStatsMapper weeklyStatsMapper;

    public WeeklyStatsService(WeeklyStatsMapper weeklyStatsMapper) {
        this.weeklyStatsMapper = weeklyStatsMapper;
    }

    @Transactional(readOnly = true)
    public WeeklyStatsResponse getWeeklyStats(String email, LocalDate targetDate) {
        LocalDate weekStart = targetDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        Map<LocalDate, WeeklyStatsResponse.DailyDietStat> dietMap = mapByDate(
                weeklyStatsMapper.aggregateDietByDate(email, weekStart, weekEnd));
        Map<LocalDate, WeeklyStatsResponse.DailyExerciseStat> exerciseMap = mapByDate(
                weeklyStatsMapper.aggregateExerciseByDate(email, weekStart, weekEnd));

        List<WeeklyStatsResponse.DailyDietStat> dietStats = new ArrayList<>();
        List<WeeklyStatsResponse.DailyExerciseStat> exerciseStats = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            dietStats.add(buildDietStat(date, dietMap.get(date)));
            exerciseStats.add(buildExerciseStat(date, exerciseMap.get(date)));
        }

        return WeeklyStatsResponse.builder()
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .dietStats(dietStats)
                .exerciseStats(exerciseStats)
                .build();
    }

    private Map<LocalDate, WeeklyStatsResponse.DailyDietStat> mapByDate(
            List<WeeklyStatsResponse.DailyDietStat> stats) {
        Map<LocalDate, WeeklyStatsResponse.DailyDietStat> map = new HashMap<>();
        for (WeeklyStatsResponse.DailyDietStat stat : stats) {
            map.put(stat.getDate(), WeeklyStatsResponse.DailyDietStat.builder()
                    .date(stat.getDate())
                    .dayOfWeek(koreanDayName(stat.getDate()))
                    .carbs(stat.getCarbs())
                    .protein(stat.getProtein())
                    .fat(stat.getFat())
                    .calories(stat.getCalories())
                    .build());
        }
        return map;
    }

    private Map<LocalDate, WeeklyStatsResponse.DailyExerciseStat> mapByDate(
            List<WeeklyStatsResponse.DailyExerciseStat> stats) {
        Map<LocalDate, WeeklyStatsResponse.DailyExerciseStat> map = new HashMap<>();
        for (WeeklyStatsResponse.DailyExerciseStat stat : stats) {
            map.put(stat.getDate(), WeeklyStatsResponse.DailyExerciseStat.builder()
                    .date(stat.getDate())
                    .dayOfWeek(koreanDayName(stat.getDate()))
                    .durationMinutes(stat.getDurationMinutes())
                    .calories(stat.getCalories())
                    .build());
        }
        return map;
    }

    private WeeklyStatsResponse.DailyDietStat buildDietStat(
            LocalDate date,
            WeeklyStatsResponse.DailyDietStat value
    ) {
        if (value != null) {
            return value;
        }
        return WeeklyStatsResponse.DailyDietStat.builder()
                .date(date)
                .dayOfWeek(koreanDayName(date))
                .carbs(0)
                .protein(0)
                .fat(0)
                .calories(0)
                .build();
    }

    private WeeklyStatsResponse.DailyExerciseStat buildExerciseStat(
            LocalDate date,
            WeeklyStatsResponse.DailyExerciseStat value
    ) {
        if (value != null) {
            return value;
        }
        return WeeklyStatsResponse.DailyExerciseStat.builder()
                .date(date)
                .dayOfWeek(koreanDayName(date))
                .durationMinutes(0)
                .calories(0)
                .build();
    }

    private String koreanDayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
    }
}
