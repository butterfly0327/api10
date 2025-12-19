package com.yumyumcoach.domain.aiadvisor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.domain.aiadvisor.dto.MealPlanResponse;
import com.yumyumcoach.domain.aiadvisor.entity.AiMealPlan;
import com.yumyumcoach.domain.aiadvisor.gemini.GeminiClient;
import com.yumyumcoach.domain.aiadvisor.mapper.AiMealPlanMapper;
import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.stats.service.WeeklyStatsService;
import com.yumyumcoach.domain.user.dto.MyPageResponse;
import com.yumyumcoach.domain.user.service.UserService;
import com.yumyumcoach.global.exception.BusinessException;
import com.yumyumcoach.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealPlanService {

    private static final ZoneId ZONE_ID_SEOUL = ZoneId.of("Asia/Seoul");
    private final AiMealPlanMapper aiMealPlanMapper;
    private final WeeklyStatsService weeklyStatsService;
    private final UserService userService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public MealPlanService(
            AiMealPlanMapper aiMealPlanMapper,
            WeeklyStatsService weeklyStatsService,
            UserService userService,
            GeminiClient geminiClient,
            ObjectMapper objectMapper
    ) {
        this.aiMealPlanMapper = aiMealPlanMapper;
        this.weeklyStatsService = weeklyStatsService;
        this.userService = userService;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MealPlanResponse generateDailyPlan(String email, LocalDate targetDate) {
        LocalDate planDate = Objects.requireNonNullElse(targetDate, LocalDate.now(ZONE_ID_SEOUL));
        AiMealPlan existing = aiMealPlanMapper.selectByEmailAndDate(email, planDate);
        if (existing != null) {
            return toResponse(existing);
        }

        WeeklyStatsResponse stats = weeklyStatsService.getWeeklyStats(email, planDate);
        MyPageResponse myPage = userService.getMyPage(email);

        String prompt = MealPlanTemplateBuilder.buildPrompt(myPage, stats, planDate);
        String aiText = geminiClient.generateContent(prompt);

        MealPlanResponse parsed = parsePlan(aiText, planDate);
        AiMealPlan entity = AiMealPlan.builder()
                .email(email)
                .planDate(planDate)
                .breakfastMenu(parsed.getBreakfastMenu())
                .lunchMenu(parsed.getLunchMenu())
                .dinnerMenu(parsed.getDinnerMenu())
                .breakfastCalories(parsed.getBreakfastCalories())
                .lunchCalories(parsed.getLunchCalories())
                .dinnerCalories(parsed.getDinnerCalories())
                .breakfastNote(parsed.getBreakfastNote())
                .lunchNote(parsed.getLunchNote())
                .dinnerNote(parsed.getDinnerNote())
                .totalCalories(parsed.getTotalCalories())
                .modelUsed(parsed.getModelUsed())
                .createdAt(LocalDateTime.now(ZONE_ID_SEOUL))
                .build();
        aiMealPlanMapper.insert(entity);
        return parsed;
    }

    public MealPlanResponse getDailyPlan(String email, LocalDate targetDate) {
        LocalDate planDate = Objects.requireNonNullElse(targetDate, LocalDate.now(ZONE_ID_SEOUL));
        AiMealPlan plan = aiMealPlanMapper.selectByEmailAndDate(email, planDate);
        if (plan == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "요청한 날짜에 저장된 식단 계획이 없습니다.");
        }
        return toResponse(plan);
    }

    private MealPlanResponse toResponse(AiMealPlan plan) {
        return MealPlanResponse.builder()
                .planDate(plan.getPlanDate())
                .breakfastMenu(plan.getBreakfastMenu())
                .lunchMenu(plan.getLunchMenu())
                .dinnerMenu(plan.getDinnerMenu())
                .breakfastCalories(plan.getBreakfastCalories())
                .lunchCalories(plan.getLunchCalories())
                .dinnerCalories(plan.getDinnerCalories())
                .breakfastNote(plan.getBreakfastNote())
                .lunchNote(plan.getLunchNote())
                .dinnerNote(plan.getDinnerNote())
                .totalCalories(plan.getTotalCalories())
                .modelUsed(plan.getModelUsed())
                .build();
    }

    private MealPlanResponse parsePlan(String aiText, LocalDate planDate) {
        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(aiText, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return MealPlanResponse.builder()
                    .planDate(planDate)
                    .breakfastMenu(aiText)
                    .lunchMenu(aiText)
                    .dinnerMenu(aiText)
                    .breakfastNote("원문 내용을 확인해 주세요.")
                    .lunchNote("원문 내용을 확인해 주세요.")
                    .dinnerNote("원문 내용을 확인해 주세요.")
                    .totalCalories(null)
                    .modelUsed("gemini-pro")
                    .build();
        }

        String breakfastMenu = getNestedText(parsed, "breakfast", "menu");
        String lunchMenu = getNestedText(parsed, "lunch", "menu");
        String dinnerMenu = getNestedText(parsed, "dinner", "menu");

        Double breakfastCalories = getNestedDouble(parsed, "breakfast", "calories");
        Double lunchCalories = getNestedDouble(parsed, "lunch", "calories");
        Double dinnerCalories = getNestedDouble(parsed, "dinner", "calories");

        String breakfastNote = getNestedText(parsed, "breakfast", "note");
        String lunchNote = getNestedText(parsed, "lunch", "note");
        String dinnerNote = getNestedText(parsed, "dinner", "note");

        Double totalCalories = null;
        if (breakfastCalories != null || lunchCalories != null || dinnerCalories != null) {
            totalCalories = nullToZero(breakfastCalories) + nullToZero(lunchCalories) + nullToZero(dinnerCalories);
        }

        return MealPlanResponse.builder()
                .planDate(planDate)
                .breakfastMenu(defaultIfBlank(breakfastMenu, ""))
                .lunchMenu(defaultIfBlank(lunchMenu, ""))
                .dinnerMenu(defaultIfBlank(dinnerMenu, ""))
                .breakfastCalories(breakfastCalories)
                .lunchCalories(lunchCalories)
                .dinnerCalories(dinnerCalories)
                .breakfastNote(defaultIfBlank(breakfastNote, ""))
                .lunchNote(defaultIfBlank(lunchNote, ""))
                .dinnerNote(defaultIfBlank(dinnerNote, ""))
                .totalCalories(totalCalories)
                .modelUsed("gemini-pro")
                .build();
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            return (Map<String, Object>) mapValue;
        }
        return Map.of("text", value == null ? "" : value.toString());
    }

    private String getNestedText(Map<String, Object> root, String parentKey, String childKey) {
        Map<String, Object> parent = asMap(root.get(parentKey));
        Object child = parent.get(childKey);
        return child == null ? "" : child.toString();
    }

    private Double getNestedDouble(Map<String, Object> root, String parentKey, String childKey) {
        Map<String, Object> parent = asMap(root.get(parentKey));
        Object child = parent.get(childKey);
        if (child instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return child == null ? null : Double.parseDouble(child.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double nullToZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
