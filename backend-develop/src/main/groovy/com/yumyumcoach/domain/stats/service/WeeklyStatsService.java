package com.yumyumcoach.domain.stats.service;

import com.yumyumcoach.domain.diet.dto.DietFoodDto;
import com.yumyumcoach.domain.diet.dto.DietRecordDto;
import com.yumyumcoach.domain.diet.service.DietRecordService;
import com.yumyumcoach.domain.exercise.dto.ExerciseRecordResponse;
import com.yumyumcoach.domain.exercise.service.ExerciseService;
import com.yumyumcoach.domain.stats.dto.WeeklyDietStat;
import com.yumyumcoach.domain.stats.dto.WeeklyExerciseStat;
import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class WeeklyStatsService {

    private final DietRecordService dietRecordService;
    private final ExerciseService exerciseService;
    private static final ZoneId ZONE_ID_SEOUL = ZoneId.of("Asia/Seoul");

    public WeeklyStatsService(
            DietRecordService dietRecordService,
            ExerciseService exerciseService
    ) {
        this.dietRecordService = dietRecordService;
        this.exerciseService = exerciseService;
    }

    public WeeklyStatsResponse getWeeklyStats(String email, LocalDate targetDate) {
        LocalDate baseDate = Objects.requireNonNullElse(targetDate, LocalDate.now(ZONE_ID_SEOUL));
        LocalDate weekStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<WeeklyDietStat> dietStats = new ArrayList<>();
        List<WeeklyExerciseStat> exerciseStats = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate current = weekStart.plusDays(i);
            String dayOfWeek = current.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            List<DietRecordDto> diets = dietRecordService.getMyDiets(email, current, 0, 500);
            double totalCalories = 0.0;
            double totalCarbs = 0.0;
            double totalProtein = 0.0;
            double totalFat = 0.0;
            for (DietRecordDto diet : diets) {
                if (diet.getItems() == null) {
                    continue;
                }
                for (DietFoodDto item : diet.getItems()) {
                    totalCalories += nullToZero(item.getCalories());
                    totalCarbs += nullToZero(item.getCarbs());
                    totalProtein += nullToZero(item.getProtein());
                    totalFat += nullToZero(item.getFat());
                }
            }
            dietStats.add(WeeklyDietStat.builder()
                    .date(current)
                    .dayOfWeekKorean(dayOfWeek)
                    .totalCalories(round(totalCalories))
                    .totalCarbs(round(totalCarbs))
                    .totalProtein(round(totalProtein))
                    .totalFat(round(totalFat))
                    .build());

            List<ExerciseRecordResponse> exercises = exerciseService.getMyExerciseRecords(email, current);
            double totalDuration = 0.0;
            double totalExerciseCalories = 0.0;
            for (ExerciseRecordResponse exercise : exercises) {
                totalDuration += nullToZero(exercise.getDurationMinutes());
                totalExerciseCalories += nullToZero(exercise.getCalories());
            }
            exerciseStats.add(WeeklyExerciseStat.builder()
                    .date(current)
                    .dayOfWeekKorean(dayOfWeek)
                    .totalDurationMinutes(round(totalDuration))
                    .totalCalories(round(totalExerciseCalories))
                    .build());
        }

        return WeeklyStatsResponse.builder()
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .dietStats(dietStats)
                .exerciseStats(exerciseStats)
                .build();
    }

    private double nullToZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
