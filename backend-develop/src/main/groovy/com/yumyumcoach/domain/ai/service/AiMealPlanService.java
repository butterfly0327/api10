package com.yumyumcoach.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.domain.ai.client.GeminiClient;
import com.yumyumcoach.domain.ai.dto.MealPlanResponse;
import com.yumyumcoach.domain.ai.entity.AiDailyMealPlan;
import com.yumyumcoach.domain.ai.mapper.AiMealPlanMapper;
import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.stats.service.WeeklyStatsService;
import com.yumyumcoach.domain.user.entity.Profile;
import com.yumyumcoach.domain.user.mapper.ProfileMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AiMealPlanService {

    private final AiMealPlanMapper aiMealPlanMapper;
    private final WeeklyStatsService weeklyStatsService;
    private final ProfileMapper profileMapper;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public AiMealPlanService(
            AiMealPlanMapper aiMealPlanMapper,
            WeeklyStatsService weeklyStatsService,
            ProfileMapper profileMapper,
            GeminiClient geminiClient,
            ObjectMapper objectMapper
    ) {
        this.aiMealPlanMapper = aiMealPlanMapper;
        this.weeklyStatsService = weeklyStatsService;
        this.profileMapper = profileMapper;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MealPlanResponse generateDailyPlan(String email, LocalDate requestDate) {
        LocalDate planDate = requestDate == null
                ? LocalDate.now(ZoneId.of("Asia/Seoul"))
                : requestDate;

        AiDailyMealPlan existing = aiMealPlanMapper.findByEmailAndDate(email, planDate);
        if (existing != null) {
            return toResponse(existing);
        }

        Profile profile = profileMapper.findByEmail(email);
        WeeklyStatsResponse weeklyStats = weeklyStatsService.getWeeklyStats(email, planDate);

        String prompt = buildPrompt(profile, weeklyStats, planDate);
        String rawMessage = geminiClient.generateText(prompt);

        AiDailyMealPlan plan = parseAndBuildPlan(email, planDate, rawMessage);
        aiMealPlanMapper.upsertMealPlan(plan);
        return toResponse(plan);
    }

    private String buildPrompt(Profile profile, WeeklyStatsResponse weeklyStats, LocalDate planDate) {
        StringBuilder builder = new StringBuilder();
        builder.append("사용자의 건강 정보를 참고하여 오늘의 아침/점심/저녁 식단을 제안해 주세요.\n")
                .append("반드시 JSON만 반환하세요. 키: model, breakfast, lunch, dinner.\n")
                .append("각 식사 객체는 menu(배열), calories(숫자), summary(한 줄 설명)를 포함하세요.\n")
                .append("응답 예: {\\\"model\\\":\\\"gemini-1.5-flash\\\", \\\"breakfast\\\":{\\\"menu\\\":[\\\"현미밥\\\"],\\\"calories\\\":500,\\\"summary\\\":\\\"단백질 중심\\\"}, ...}\n\n");

        builder.append("오늘 날짜: ").append(planDate).append(" (")
                .append(planDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN))
                .append(")\n");

        if (profile != null) {
            builder.append("[건강 정보]\n")
                    .append("- 키: ").append(profile.getHeight()).append("cm\n")
                    .append("- 현재 체중: ").append(profile.getCurrentWeight()).append("kg, 목표 체중: ")
                    .append(profile.getTargetWeight()).append("kg\n")
                    .append("- 당뇨: ").append(profile.getHasDiabetes()).append(", 고혈압: ")
                    .append(profile.getHasHypertension()).append(", 고지혈증: ")
                    .append(profile.getHasHyperlipidemia()).append("\n")
                    .append("- 기타 질환: ").append(profile.getOtherDisease()).append("\n")
                    .append("- 목표: ").append(profile.getGoal()).append("\n")
                    .append("- 활동량 레벨: ").append(profile.getActivityLevel()).append("\n\n");
        }

        builder.append("[주간 식단/운동 요약]\n");
        weeklyStats.getDietStats().forEach(stat -> builder.append("- ")
                .append(stat.getDate()).append(" ")
                .append(stat.getDayOfWeek()).append(" : 탄수화물 ")
                .append(stat.getCarbs()).append("g, 단백질 ")
                .append(stat.getProtein()).append("g, 지방 ")
                .append(stat.getFat()).append("g, 열량 ")
                .append(stat.getCalories()).append("kcal\n"));
        weeklyStats.getExerciseStats().forEach(stat -> builder.append("- 운동 ")
                .append(stat.getDate()).append(" ")
                .append(stat.getDayOfWeek()).append(" : 시간 ")
                .append(stat.getDurationMinutes()).append("분, 소모칼로리 ")
                .append(stat.getCalories()).append("kcal\n"));

        builder.append("사용자당 하루 한 번만 호출하므로 중복된 계획이 아니라면 새로운 제안을 해주세요.");
        return builder.toString();
    }

    private AiDailyMealPlan parseAndBuildPlan(String email, LocalDate planDate, String rawMessage) {
        String model = "gemini";
        String breakfastMenu = null;
        Double breakfastCalories = null;
        String breakfastSummary = null;
        String lunchMenu = null;
        Double lunchCalories = null;
        String lunchSummary = null;
        String dinnerMenu = null;
        Double dinnerCalories = null;
        String dinnerSummary = null;

        try {
            JsonNode root = objectMapper.readTree(rawMessage);
            if (root.hasNonNull("model")) {
                model = root.path("model").asText(model);
            }
            ParsedMeal breakfast = parseMeal(root.path("breakfast"));
            ParsedMeal lunch = parseMeal(root.path("lunch"));
            ParsedMeal dinner = parseMeal(root.path("dinner"));

            breakfastMenu = breakfast.menu;
            breakfastCalories = breakfast.calories;
            breakfastSummary = breakfast.summary;
            lunchMenu = lunch.menu;
            lunchCalories = lunch.calories;
            lunchSummary = lunch.summary;
            dinnerMenu = dinner.menu;
            dinnerCalories = dinner.calories;
            dinnerSummary = dinner.summary;
        } catch (JsonProcessingException ignored) {
            // raw message will be preserved
        }

        return AiDailyMealPlan.builder()
                .email(email)
                .planDate(planDate)
                .model(model)
                .breakfastMenu(breakfastMenu)
                .breakfastCalories(breakfastCalories)
                .breakfastSummary(breakfastSummary)
                .lunchMenu(lunchMenu)
                .lunchCalories(lunchCalories)
                .lunchSummary(lunchSummary)
                .dinnerMenu(dinnerMenu)
                .dinnerCalories(dinnerCalories)
                .dinnerSummary(dinnerSummary)
                .rawResponse(rawMessage)
                .requestContext(buildContextSnapshot(planDate))
                .build();
    }

    private String buildContextSnapshot(LocalDate planDate) {
        return "planDate=" + planDate;
    }

    private ParsedMeal parseMeal(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return new ParsedMeal(null, null, null);
        }
        String menuText = null;
        if (node.has("menu") && node.get("menu").isArray()) {
            List<String> items = new ArrayList<>();
            node.get("menu").forEach(n -> items.add(n.asText()));
            menuText = String.join(", ", items);
        } else if (node.has("items")) {
            List<String> items = new ArrayList<>();
            node.get("items").forEach(n -> items.add(n.asText()));
            menuText = String.join(", ", items);
        } else if (node.has("menu")) {
            menuText = node.get("menu").asText();
        }
        Double calories = node.has("calories") && node.get("calories").isNumber()
                ? node.get("calories").asDouble()
                : null;
        String summary = node.has("summary") ? node.get("summary").asText(null) : null;
        if (!StringUtils.hasText(summary) && node.has("note")) {
            summary = node.get("note").asText();
        }
        return new ParsedMeal(menuText, calories, summary);
    }

    private MealPlanResponse toResponse(AiDailyMealPlan plan) {
        List<MealPlanResponse.MealItem> meals = new ArrayList<>();
        meals.add(toMealItem("breakfast", plan.getBreakfastMenu(), plan.getBreakfastCalories(), plan.getBreakfastSummary()));
        meals.add(toMealItem("lunch", plan.getLunchMenu(), plan.getLunchCalories(), plan.getLunchSummary()));
        meals.add(toMealItem("dinner", plan.getDinnerMenu(), plan.getDinnerCalories(), plan.getDinnerSummary()));

        return MealPlanResponse.builder()
                .planId(plan.getId())
                .date(plan.getPlanDate())
                .dayOfWeek(plan.getPlanDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN))
                .model(plan.getModel())
                .meals(meals)
                .rawMessage(plan.getRawResponse())
                .build();
    }

    private MealPlanResponse.MealItem toMealItem(String mealType, String menu, Double calories, String note) {
        return MealPlanResponse.MealItem.builder()
                .mealType(mealType)
                .menu(menu)
                .calories(calories)
                .note(note)
                .build();
    }

    private record ParsedMeal(String menu, Double calories, String summary) {
    }
}
