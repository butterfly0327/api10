package com.yumyumcoach.domain.aiadvisor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.domain.aiadvisor.dto.NutritionEvaluationResponse;
import com.yumyumcoach.domain.aiadvisor.entity.AiNutritionEvaluation;
import com.yumyumcoach.domain.aiadvisor.gemini.GeminiClient;
import com.yumyumcoach.domain.aiadvisor.mapper.AiNutritionEvaluationMapper;
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
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NutritionEvaluationService {

    private static final ZoneId ZONE_ID_SEOUL = ZoneId.of("Asia/Seoul");
    private final AiNutritionEvaluationMapper aiNutritionEvaluationMapper;
    private final WeeklyStatsService weeklyStatsService;
    private final UserService userService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public NutritionEvaluationService(
            AiNutritionEvaluationMapper aiNutritionEvaluationMapper,
            WeeklyStatsService weeklyStatsService,
            UserService userService,
            GeminiClient geminiClient,
            ObjectMapper objectMapper
    ) {
        this.aiNutritionEvaluationMapper = aiNutritionEvaluationMapper;
        this.weeklyStatsService = weeklyStatsService;
        this.userService = userService;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public NutritionEvaluationResponse evaluate(String email, LocalDate targetDate) {
        LocalDate referenceDate = Objects.requireNonNullElse(targetDate, LocalDate.now(ZONE_ID_SEOUL));
        AiNutritionEvaluation existing = aiNutritionEvaluationMapper.selectByEmailAndReferenceDate(email, referenceDate);
        if (existing != null) {
            return toResponse(existing);
        }

        WeeklyStatsResponse stats = weeklyStatsService.getWeeklyStats(email, referenceDate);
        MyPageResponse myPage = userService.getMyPage(email);
        WeeklyStatsResponse trimmed = trimToDate(stats, referenceDate);

        String prompt = NutritionTemplateBuilder.buildPrompt(myPage, trimmed, referenceDate, LocalDateTime.now(ZONE_ID_SEOUL));
        String aiText = geminiClient.generateContent(prompt);
        NutritionEvaluationResponse parsed = parseEvaluation(aiText, referenceDate, trimmed);

        AiNutritionEvaluation entity = AiNutritionEvaluation.builder()
                .email(email)
                .referenceDate(referenceDate)
                .weekStartDate(trimmed.getWeekStartDate())
                .weekEndDate(trimmed.getWeekEndDate())
                .carbohydrateStatus(parsed.getCarbohydrateStatus())
                .proteinStatus(parsed.getProteinStatus())
                .fatStatus(parsed.getFatStatus())
                .calorieStatus(parsed.getCalorieStatus())
                .analysisSummary(parsed.getAnalysisSummary())
                .modelUsed(parsed.getModelUsed())
                .createdAt(LocalDateTime.now(ZONE_ID_SEOUL))
                .build();
        aiNutritionEvaluationMapper.insert(entity);
        return parsed;
    }

    public NutritionEvaluationResponse getEvaluation(String email, LocalDate targetDate) {
        LocalDate referenceDate = Objects.requireNonNullElse(targetDate, LocalDate.now(ZONE_ID_SEOUL));
        AiNutritionEvaluation evaluation = aiNutritionEvaluationMapper.selectByEmailAndReferenceDate(email, referenceDate);
        if (evaluation == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "해당 날짜의 영양 평가가 없습니다.");
        }
        return toResponse(evaluation);
    }

    private NutritionEvaluationResponse parseEvaluation(String aiText, LocalDate referenceDate, WeeklyStatsResponse stats) {
        try {
            Map<String, Object> parsed = objectMapper.readValue(aiText, new TypeReference<Map<String, Object>>() {
            });
            return NutritionEvaluationResponse.builder()
                    .referenceDate(referenceDate)
                    .weekStartDate(stats.getWeekStartDate())
                    .weekEndDate(stats.getWeekEndDate())
                    .carbohydrateStatus(asText(parsed.get("carbohydrateStatus")))
                    .proteinStatus(asText(parsed.get("proteinStatus")))
                    .fatStatus(asText(parsed.get("fatStatus")))
                    .calorieStatus(asText(parsed.get("calorieStatus")))
                    .analysisSummary(asText(parsed.get("analysisSummary")))
                    .modelUsed("gemini-pro")
                    .build();
        } catch (Exception e) {
            return NutritionEvaluationResponse.builder()
                    .referenceDate(referenceDate)
                    .weekStartDate(stats.getWeekStartDate())
                    .weekEndDate(stats.getWeekEndDate())
                    .carbohydrateStatus("결과 확인 필요")
                    .proteinStatus("결과 확인 필요")
                    .fatStatus("결과 확인 필요")
                    .calorieStatus("결과 확인 필요")
                    .analysisSummary(aiText)
                    .modelUsed("gemini-pro")
                    .build();
        }
    }

    private WeeklyStatsResponse trimToDate(WeeklyStatsResponse stats, LocalDate referenceDate) {
        return WeeklyStatsResponse.builder()
                .weekStartDate(stats.getWeekStartDate())
                .weekEndDate(stats.getWeekEndDate())
                .dietStats(stats.getDietStats().stream()
                        .filter(d -> !d.getDate().isAfter(referenceDate))
                        .collect(Collectors.toList()))
                .exerciseStats(stats.getExerciseStats().stream()
                        .filter(e -> !e.getDate().isAfter(referenceDate))
                        .collect(Collectors.toList()))
                .build();
    }

    private String asText(Object value) {
        return value == null ? "" : value.toString();
    }

    private NutritionEvaluationResponse toResponse(AiNutritionEvaluation entity) {
        return NutritionEvaluationResponse.builder()
                .referenceDate(entity.getReferenceDate())
                .weekStartDate(entity.getWeekStartDate())
                .weekEndDate(entity.getWeekEndDate())
                .carbohydrateStatus(entity.getCarbohydrateStatus())
                .proteinStatus(entity.getProteinStatus())
                .fatStatus(entity.getFatStatus())
                .calorieStatus(entity.getCalorieStatus())
                .analysisSummary(entity.getAnalysisSummary())
                .modelUsed(entity.getModelUsed())
                .build();
    }
}
