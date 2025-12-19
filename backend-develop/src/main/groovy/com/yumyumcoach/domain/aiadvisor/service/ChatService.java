package com.yumyumcoach.domain.aiadvisor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.domain.aiadvisor.dto.ChatMessageResponse;
import com.yumyumcoach.domain.aiadvisor.dto.ChatSendRequest;
import com.yumyumcoach.domain.aiadvisor.entity.AiChatMessage;
import com.yumyumcoach.domain.aiadvisor.gemini.GeminiClient;
import com.yumyumcoach.domain.aiadvisor.mapper.AiChatMessageMapper;
import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.stats.service.WeeklyStatsService;
import com.yumyumcoach.domain.user.dto.MyPageResponse;
import com.yumyumcoach.domain.user.service.UserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private static final ZoneId ZONE_ID_SEOUL = ZoneId.of("Asia/Seoul");
    private final AiChatMessageMapper aiChatMessageMapper;
    private final GeminiClient geminiClient;
    private final WeeklyStatsService weeklyStatsService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public ChatService(
            AiChatMessageMapper aiChatMessageMapper,
            GeminiClient geminiClient,
            WeeklyStatsService weeklyStatsService,
            UserService userService,
            ObjectMapper objectMapper
    ) {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.geminiClient = geminiClient;
        this.weeklyStatsService = weeklyStatsService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<ChatMessageResponse> sendMessage(String email, ChatSendRequest request) {
        LocalDate conversationDate = Objects.requireNonNullElse(request.getConversationDate(), LocalDate.now(ZONE_ID_SEOUL));

        String prompt = buildChatPrompt(email, conversationDate, request.getMessage());
        String aiAnswer = geminiClient.generateContent(prompt);

        AiChatMessage userMessage = AiChatMessage.builder()
                .email(email)
                .conversationDate(conversationDate)
                .role("USER")
                .message(request.getMessage())
                .createdAt(LocalDateTime.now(ZONE_ID_SEOUL))
                .build();
        aiChatMessageMapper.insert(userMessage);

        AiChatMessage botMessage = AiChatMessage.builder()
                .email(email)
                .conversationDate(conversationDate)
                .role("ASSISTANT")
                .message(aiAnswer)
                .createdAt(LocalDateTime.now(ZONE_ID_SEOUL))
                .build();
        aiChatMessageMapper.insert(botMessage);

        return aiChatMessageMapper.selectByEmailAndDate(email, conversationDate).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ChatMessageResponse> getHistory(String email, LocalDate conversationDate) {
        LocalDate targetDate = Objects.requireNonNullElse(conversationDate, LocalDate.now(ZONE_ID_SEOUL));
        return aiChatMessageMapper.selectByEmailAndDate(email, targetDate).stream()
                .map(this::toResponse)
                .toList();
    }

    private String buildChatPrompt(String email, LocalDate conversationDate, String message) {
        WeeklyStatsResponse stats = weeklyStatsService.getWeeklyStats(email, conversationDate);
        MyPageResponse myPage = userService.getMyPage(email);

        String statsJson;
        try {
            statsJson = objectMapper.writeValueAsString(stats);
        } catch (Exception e) {
            statsJson = stats.toString();
        }

        String healthJson;
        try {
            healthJson = objectMapper.writeValueAsString(myPage);
        } catch (Exception e) {
            healthJson = myPage.toString();
        }

        return "다음은 사용자의 건강 정보와 일주일 식단/운동 기록입니다. 이 정보를 참고하여 사용자의 질문에 답변해주세요. " +
                "이전 대화 내용은 전달되지 않으니, 주어진 정보와 질문만으로 답변을 생성하세요. " +
                "사용자가 이해하기 쉬운 한국어로 답변해주세요.\n" +
                "대화 날짜: " + conversationDate + "\n" +
                "사용자 건강 정보: " + healthJson + "\n" +
                "주간 통계: " + statsJson + "\n" +
                "사용자 질문: " + message;
    }

    private ChatMessageResponse toResponse(AiChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .conversationDate(message.getConversationDate())
                .role(message.getRole())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
