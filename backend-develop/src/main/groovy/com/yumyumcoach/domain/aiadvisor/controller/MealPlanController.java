package com.yumyumcoach.domain.aiadvisor.controller;

import com.yumyumcoach.domain.aiadvisor.dto.GenerateMealPlanRequest;
import com.yumyumcoach.domain.aiadvisor.dto.MealPlanResponse;
import com.yumyumcoach.domain.aiadvisor.service.MealPlanService;
import com.yumyumcoach.global.common.CurrentUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/ai-meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping("/generate-today")
    public MealPlanResponse generateMealPlan(@RequestBody @Valid GenerateMealPlanRequest request) {
        String email = CurrentUser.email();
        return mealPlanService.generateDailyPlan(email, request.getTargetDate());
    }

    @GetMapping("/by-date")
    public MealPlanResponse getMealPlan(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = CurrentUser.email();
        return mealPlanService.getDailyPlan(email, date);
    }
}
