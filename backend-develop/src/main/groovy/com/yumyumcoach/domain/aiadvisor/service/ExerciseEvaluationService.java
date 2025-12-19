package com.yumyumcoach.domain.aiadvisor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.domain.aiadvisor.dto.ExerciseEvaluationResponse;
import com.yumyumcoach.domain.aiadvisor.entity.AiExerciseEvaluation;
import com.yumyumcoach.domain.aiadvisor.gemini.GeminiClient;
import com.yumyumcoach.domain.aiadvisor.mapper.AiExerciseEvaluationMapper;
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
public class ExerciseEvaluationService {

    private static final ZoneId ZONE_ID_SEOUL = ZoneId.of("Asia/Seoul");
    private final AiExerciseEvaluationMapper aiExerciseEvaluationMapper;
    private final WeeklyStatsService weeklyStatsService;
    private final UserService userService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public ExerciseEvaluationService(
            AiExerciseEvaluationMapper aiExerciseEvaluationMapper,
            WeeklyStatsService weeklyStatsService,
            UserService userService,
            GeminiClient geminiClient,
            ObjectMapper objectMapper
    ) {
        this.aiExerciseEvaluationMapper = aiExerciseEvaluationMapper;
        this.weeklyStatsService = weeklyStatsService;
        this.userService = userService;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ExerciseEvaluationResponse evaluate(String email, LocalDate targetDate) {
        LocalDate referenceDate = Objects.requireNonNullElse(targetDate, LocalDate.now(ZONE_ID_SEOUL));
        AiExerciseEvaluation existing = aiExerciseEvaluationMapper.selectByEmailAndReferenceDate(email, referenceDate);
        if (existing != null) {
            return toResponse(existing);
        }

        WeeklyStatsResponse stats = weeklyStatsService.getWeeklyStats(email, referenceDate);
        WeeklyStatsResponse trimmed = trimToDate(stats, referenceDate);
        MyPageResponse myPage = userService.getMyPage(email);

        String prompt = ExerciseTemplateBuilder.buildPrompt(myPage, trimmed, referenceDate);
        String aiText = geminiClient.generateContent(prompt);
        ExerciseEvaluationResponse parsed = parseEvaluation(aiText, referenceDate, trimmed);

        AiExerciseEvaluation entity = AiExerciseEvaluation.builder()
                .email(email)
                .referenceDate(referenceDate)
                .weekStartDate(trimmed.getWeekStartDate())
                .weekEndDate(trimmed.getWeekEndDate())
                .volumeStatus(parsed.getVolumeStatus())
                .recommendation(parsed.getRecommendation())
                .modelUsed(parsed.getModelUsed())
                .createdAt(LocalDateTime.now(ZONE_ID_SEOUL))
                .build();
        aiExerciseEvaluationMapper.insert(entity);
        return parsed;
    }

    public ExerciseEvaluationResponse getEvaluation(String email, LocalDate targetDate) {
        LocalDate referenceDate = Objects.requireNonNullElse(targetDate, LocalDate.now(ZONE_ID_SEOUL));
        AiExerciseEvaluation evaluation = aiExerciseEvaluationMapper.selectByEmailAndReferenceDate(email, referenceDate);
        if (evaluation == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "해당 날짜의 운동 평가가 없습니다.");
        }
        return toResponse(evaluation);
    }

    private ExerciseEvaluationResponse parseEvaluation(String aiText, LocalDate referenceDate, WeeklyStatsResponse stats) {
        try {
            Map<String, Object> parsed = objectMapper.readValue(aiText, new TypeReference<Map<String, Object>>() {
            });
            return ExerciseEvaluationResponse.builder()
                    .referenceDate(referenceDate)
                    .weekStartDate(stats.getWeekStartDate())
                    .weekEndDate(stats.getWeekEndDate())
                    .volumeStatus(asText(parsed.get("volumeStatus")))
                    .recommendation(asText(parsed.get("recommendation")))
                    .modelUsed("gemini-pro")
                    .build();
        } catch (Exception e) {
            return ExerciseEvaluationResponse.builder()
                    .referenceDate(referenceDate)
                    .weekStartDate(stats.getWeekStartDate())
                    .weekEndDate(stats.getWeekEndDate())
                    .volumeStatus("결과 확인 필요")
                    .recommendation(aiText)
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

    private ExerciseEvaluationResponse toResponse(AiExerciseEvaluation entity) {
        return ExerciseEvaluationResponse.builder()
                .referenceDate(entity.getReferenceDate())
                .weekStartDate(entity.getWeekStartDate())
                .weekEndDate(entity.getWeekEndDate())
                .volumeStatus(entity.getVolumeStatus())
                .recommendation(entity.getRecommendation())
                .modelUsed(entity.getModelUsed())
                .build();
    }
}
