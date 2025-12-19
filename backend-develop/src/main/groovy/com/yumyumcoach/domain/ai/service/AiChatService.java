package com.yumyumcoach.domain.ai.service;

import com.yumyumcoach.domain.ai.client.GeminiClient;
import com.yumyumcoach.domain.ai.dto.ChatMessageResponse;
import com.yumyumcoach.domain.ai.dto.ChatRequest;
import com.yumyumcoach.domain.ai.entity.AiChatMessage;
import com.yumyumcoach.domain.ai.mapper.AiChatMessageMapper;
import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.stats.service.WeeklyStatsService;
import com.yumyumcoach.domain.user.entity.Profile;
import com.yumyumcoach.domain.user.mapper.ProfileMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiChatService {

    private final AiChatMessageMapper aiChatMessageMapper;
    private final WeeklyStatsService weeklyStatsService;
    private final ProfileMapper profileMapper;
    private final GeminiClient geminiClient;

    public AiChatService(
            AiChatMessageMapper aiChatMessageMapper,
            WeeklyStatsService weeklyStatsService,
            ProfileMapper profileMapper,
            GeminiClient geminiClient
    ) {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.weeklyStatsService = weeklyStatsService;
        this.profileMapper = profileMapper;
        this.geminiClient = geminiClient;
    }

    @Transactional
    public ChatMessageResponse ask(String email, ChatRequest request) {
        LocalDate conversationDate = request.getConversationDate() != null
                ? request.getConversationDate()
                : LocalDate.now(ZoneId.of("Asia/Seoul"));

        Profile profile = profileMapper.findByEmail(email);
        WeeklyStatsResponse weeklyStats = weeklyStatsService.getWeeklyStats(email, conversationDate);

        String context = buildContext(profile, weeklyStats, conversationDate);
        String question = request.getMessage();
        if (question == null || question.isBlank()) {
            question = "오늘의 건강/운동 조언을 알려주세요.";
        }
        String prompt = context + "\n[사용자 질문]\n" + question + "\nJSON이나 복잡한 포맷 없이 한국어로 답변하세요.";
        String answer = geminiClient.generateText(prompt);

        AiChatMessage userMessage = AiChatMessage.builder()
                .email(email)
                .conversationDate(conversationDate)
                .role("USER")
                .message(question)
                .requestContext(context)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
        aiChatMessageMapper.insert(userMessage);

        AiChatMessage botMessage = AiChatMessage.builder()
                .email(email)
                .conversationDate(conversationDate)
                .role("BOT")
                .message(answer)
                .requestContext(context)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
        aiChatMessageMapper.insert(botMessage);

        return ChatMessageResponse.builder()
                .messageId(botMessage.getId())
                .role(botMessage.getRole())
                .content(botMessage.getMessage())
                .conversationDate(botMessage.getConversationDate())
                .createdAt(botMessage.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(String email, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now(ZoneId.of("Asia/Seoul"));
        return aiChatMessageMapper.findByEmailAndDate(email, targetDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String buildContext(Profile profile, WeeklyStatsResponse weeklyStats, LocalDate date) {
        StringBuilder builder = new StringBuilder();
        builder.append("[사용자/상황 정보]\n")
                .append("- 오늘 날짜: ").append(date).append(" (")
                .append(date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN)).append(")\n");
        if (profile != null) {
            builder.append("- 키: ").append(profile.getHeight()).append("cm, 현재 체중: ")
                    .append(profile.getCurrentWeight()).append("kg, 목표 체중: ")
                    .append(profile.getTargetWeight()).append("kg\n")
                    .append("- 질환: 당뇨=").append(profile.getHasDiabetes())
                    .append(", 고혈압=").append(profile.getHasHypertension())
                    .append(", 고지혈증=").append(profile.getHasHyperlipidemia())
                    .append(" 기타=").append(profile.getOtherDisease()).append("\n")
                    .append("- 목표/활동량: ").append(profile.getGoal())
                    .append(", 활동량=").append(profile.getActivityLevel()).append("\n");
        }
        builder.append("[주간 식단 요약]\n");
        weeklyStats.getDietStats().forEach(stat -> builder.append("- ")
                .append(stat.getDate()).append(" : 칼로리 ")
                .append(stat.getCalories()).append("kcal, 탄수화물 ")
                .append(stat.getCarbs()).append("g, 단백질 ")
                .append(stat.getProtein()).append("g, 지방 ")
                .append(stat.getFat()).append("g\n"));
        builder.append("[주간 운동 요약]\n");
        weeklyStats.getExerciseStats().forEach(stat -> builder.append("- ")
                .append(stat.getDate()).append(" : 운동시간 ")
                .append(stat.getDurationMinutes()).append("분, 소모칼로리 ")
                .append(stat.getCalories()).append("kcal\n"));
        return builder.toString();
    }

    private ChatMessageResponse toResponse(AiChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .role(message.getRole())
                .content(message.getMessage())
                .conversationDate(message.getConversationDate())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
