package com.yumyumcoach.domain.ai.controller;

import com.yumyumcoach.domain.ai.dto.ChatMessageResponse;
import com.yumyumcoach.domain.ai.dto.ChatRequest;
import com.yumyumcoach.domain.ai.dto.GenerateMealPlanRequest;
import com.yumyumcoach.domain.ai.dto.MealPlanResponse;
import com.yumyumcoach.domain.ai.dto.WeeklyExerciseReviewResponse;
import com.yumyumcoach.domain.ai.dto.WeeklyNutritionReviewResponse;
import com.yumyumcoach.domain.ai.dto.WeeklyReviewRequest;
import com.yumyumcoach.domain.ai.service.AiChatService;
import com.yumyumcoach.domain.ai.service.AiMealPlanService;
import com.yumyumcoach.domain.ai.service.AiWeeklyReviewService;
import com.yumyumcoach.global.common.CurrentUser;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/ai")
public class AiController {

    private final AiMealPlanService aiMealPlanService;
    private final AiChatService aiChatService;
    private final AiWeeklyReviewService aiWeeklyReviewService;

    public AiController(
            AiMealPlanService aiMealPlanService,
            AiChatService aiChatService,
            AiWeeklyReviewService aiWeeklyReviewService
    ) {
        this.aiMealPlanService = aiMealPlanService;
        this.aiChatService = aiChatService;
        this.aiWeeklyReviewService = aiWeeklyReviewService;
    }

    @PostMapping("/meal-plans")
    public MealPlanResponse generateMealPlan(@RequestBody(required = false) GenerateMealPlanRequest request) {
        String email = CurrentUser.email();
        LocalDate date = request != null ? request.getDate() : null;
        return aiMealPlanService.generateDailyPlan(email, date);
    }

    @PostMapping("/chats")
    public ChatMessageResponse askChat(@RequestBody(required = false) ChatRequest request) {
        String email = CurrentUser.email();
        return aiChatService.ask(email, request == null ? new ChatRequest() : request);
    }

    @GetMapping("/chats")
    public List<ChatMessageResponse> getChatHistory(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = CurrentUser.email();
        return aiChatService.getHistory(email, date);
    }

    @PostMapping("/nutrition-reviews")
    public WeeklyNutritionReviewResponse reviewNutrition(@RequestBody(required = false) WeeklyReviewRequest request) {
        String email = CurrentUser.email();
        LocalDate date = request != null ? request.getDate() : null;
        return aiWeeklyReviewService.reviewNutrition(email, date);
    }

    @PostMapping("/exercise-reviews")
    public WeeklyExerciseReviewResponse reviewExercise(@RequestBody(required = false) WeeklyReviewRequest request) {
        String email = CurrentUser.email();
        LocalDate date = request != null ? request.getDate() : null;
        return aiWeeklyReviewService.reviewExercise(email, date);
    }
}
