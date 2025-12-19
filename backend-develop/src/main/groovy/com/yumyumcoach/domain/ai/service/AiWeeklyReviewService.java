package com.yumyumcoach.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.domain.ai.client.GeminiClient;
import com.yumyumcoach.domain.ai.dto.WeeklyExerciseReviewResponse;
import com.yumyumcoach.domain.ai.dto.WeeklyNutritionReviewResponse;
import com.yumyumcoach.domain.ai.entity.AiWeeklyExerciseReview;
import com.yumyumcoach.domain.ai.entity.AiWeeklyNutritionReview;
import com.yumyumcoach.domain.ai.mapper.AiWeeklyReviewMapper;
import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.stats.service.WeeklyStatsService;
import com.yumyumcoach.domain.user.entity.Profile;
import com.yumyumcoach.domain.user.mapper.ProfileMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AiWeeklyReviewService {

    private final AiWeeklyReviewMapper aiWeeklyReviewMapper;
    private final WeeklyStatsService weeklyStatsService;
    private final ProfileMapper profileMapper;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public AiWeeklyReviewService(
            AiWeeklyReviewMapper aiWeeklyReviewMapper,
            WeeklyStatsService weeklyStatsService,
            ProfileMapper profileMapper,
            GeminiClient geminiClient,
            ObjectMapper objectMapper
    ) {
        this.aiWeeklyReviewMapper = aiWeeklyReviewMapper;
        this.weeklyStatsService = weeklyStatsService;
        this.profileMapper = profileMapper;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public WeeklyNutritionReviewResponse reviewNutrition(String email, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(ZoneId.of("Asia/Seoul"));
        AiWeeklyNutritionReview existing = aiWeeklyReviewMapper.findNutritionByEmailAndDate(email, targetDate);
        if (existing != null) {
            return toResponse(existing);
        }

        Profile profile = profileMapper.findByEmail(email);
        WeeklyStatsResponse weeklyStats = weeklyStatsService.getWeeklyStats(email, targetDate);

        String prompt = buildNutritionPrompt(profile, weeklyStats, targetDate);
        String message = geminiClient.generateText(prompt);

        AiWeeklyNutritionReview review = AiWeeklyNutritionReview.builder()
                .email(email)
                .targetDate(targetDate)
                .weekStartDate(targetDate.with(DayOfWeek.MONDAY))
                .weekEndDate(targetDate.with(DayOfWeek.MONDAY).plusDays(6))
                .model(extractModel(message))
                .nutritionAssessment(extractNodeText(message, "assessment"))
                .guidance(extractNodeText(message, "guidance"))
                .requestContext("nutrition-date=" + targetDate)
                .build();
        aiWeeklyReviewMapper.upsertNutrition(review);
        return toResponse(review);
    }

    @Transactional
    public WeeklyExerciseReviewResponse reviewExercise(String email, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(ZoneId.of("Asia/Seoul"));
        AiWeeklyExerciseReview existing = aiWeeklyReviewMapper.findExerciseByEmailAndDate(email, targetDate);
        if (existing != null) {
            return toResponse(existing);
        }

        Profile profile = profileMapper.findByEmail(email);
        WeeklyStatsResponse weeklyStats = weeklyStatsService.getWeeklyStats(email, targetDate);

        String prompt = buildExercisePrompt(profile, weeklyStats, targetDate);
        String message = geminiClient.generateText(prompt);

        AiWeeklyExerciseReview review = AiWeeklyExerciseReview.builder()
                .email(email)
                .targetDate(targetDate)
                .weekStartDate(targetDate.with(DayOfWeek.MONDAY))
                .weekEndDate(targetDate.with(DayOfWeek.MONDAY).plusDays(6))
                .model(extractModel(message))
                .exerciseAssessment(extractNodeText(message, "assessment"))
                .recommendation(extractNodeText(message, "recommendation"))
                .requestContext("exercise-date=" + targetDate)
                .build();
        aiWeeklyReviewMapper.upsertExercise(review);
        return toResponse(review);
    }

    private String buildNutritionPrompt(Profile profile, WeeklyStatsResponse stats, LocalDate targetDate) {
        StringBuilder builder = new StringBuilder();
        builder.append("주어진 주간 식단 정보를 기반으로 탄수화물/단백질/지방/칼로리에 대해 부족/적당/과다 중 하나로 평가하고, 짧은 코멘트를 제공하세요.\n")
                .append("JSON으로만 응답하고 assessment(문장 또는 요약), guidance(추가 설명) 필드를 포함하세요.\n");
        appendSharedContext(builder, profile, targetDate);
        builder.append("[이번 주 식단 데이터]\n");
        stats.getDietStats().stream()
                .filter(stat -> !stat.getDate().isAfter(targetDate))
                .forEach(stat -> builder.append("- ")
                        .append(stat.getDate()).append(" (")
                        .append(stat.getDayOfWeek()).append(") : 칼로리 ")
                        .append(stat.getCalories()).append("kcal, 탄수화물 ")
                        .append(stat.getCarbs()).append("g, 단백질 ")
                        .append(stat.getProtein()).append("g, 지방 ")
                        .append(stat.getFat()).append("g\n"));
        return builder.toString();
    }

    private String buildExercisePrompt(Profile profile, WeeklyStatsResponse stats, LocalDate targetDate) {
        StringBuilder builder = new StringBuilder();
        builder.append("주어진 주간 운동량을 토대로 부족/적당/많음 중 하나를 선택하고, 추천 운동을 제안하세요.\n")
                .append("JSON으로만 응답하며 assessment와 recommendation 키를 포함하세요.\n");
        appendSharedContext(builder, profile, targetDate);
        builder.append("[이번 주 운동 데이터]\n");
        stats.getExerciseStats().stream()
                .filter(stat -> !stat.getDate().isAfter(targetDate))
                .forEach(stat -> builder.append("- ")
                        .append(stat.getDate()).append(" (")
                        .append(stat.getDayOfWeek()).append(") : 운동시간 ")
                        .append(stat.getDurationMinutes()).append("분, 소모칼로리 ")
                        .append(stat.getCalories()).append("kcal\n"));
        return builder.toString();
    }

    private void appendSharedContext(StringBuilder builder, Profile profile, LocalDate targetDate) {
        builder.append("[사용자 정보]\n")
                .append("- 기준 날짜: ").append(targetDate).append(" (")
                .append(targetDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN))
                .append(")\n");
        if (profile != null) {
            builder.append("- 키/체중/목표체중: ").append(profile.getHeight()).append("cm / ")
                    .append(profile.getCurrentWeight()).append("kg / ")
                    .append(profile.getTargetWeight()).append("kg\n")
                    .append("- 건강 상태: 당뇨=").append(profile.getHasDiabetes())
                    .append(", 고혈압=").append(profile.getHasHypertension())
                    .append(", 고지혈증=").append(profile.getHasHyperlipidemia())
                    .append(", 기타=").append(profile.getOtherDisease()).append("\n")
                    .append("- 목표/활동량: ").append(profile.getGoal())
                    .append(" / ").append(profile.getActivityLevel()).append("\n");
        }
    }

    private String extractNodeText(String message, String field) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode node = root.path(field);
            if (node.isObject() || node.isArray()) {
                return node.toString();
            }
            String value = node.asText(null);
            if (StringUtils.hasText(value)) {
                return value;
            }
        } catch (Exception ignored) {
        }
        return message;
    }

    private String extractModel(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            if (root.has("model")) {
                return root.get("model").asText();
            }
        } catch (Exception ignored) {
        }
        return "gemini";
    }

    private WeeklyNutritionReviewResponse toResponse(AiWeeklyNutritionReview review) {
        return WeeklyNutritionReviewResponse.builder()
                .reviewId(review.getId())
                .targetDate(review.getTargetDate())
                .weekStartDate(review.getWeekStartDate())
                .weekEndDate(review.getWeekEndDate())
                .assessment(review.getNutritionAssessment())
                .guidance(review.getGuidance())
                .model(review.getModel())
                .rawMessage(review.getNutritionAssessment())
                .build();
    }

    private WeeklyExerciseReviewResponse toResponse(AiWeeklyExerciseReview review) {
        return WeeklyExerciseReviewResponse.builder()
                .reviewId(review.getId())
                .targetDate(review.getTargetDate())
                .weekStartDate(review.getWeekStartDate())
                .weekEndDate(review.getWeekEndDate())
                .assessment(review.getExerciseAssessment())
                .recommendation(review.getRecommendation())
                .model(review.getModel())
                .rawMessage(review.getExerciseAssessment())
                .build();
    }
}
