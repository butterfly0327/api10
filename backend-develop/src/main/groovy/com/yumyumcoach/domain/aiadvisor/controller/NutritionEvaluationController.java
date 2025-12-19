package com.yumyumcoach.domain.aiadvisor.controller;

import com.yumyumcoach.domain.aiadvisor.dto.GenerateEvaluationRequest;
import com.yumyumcoach.domain.aiadvisor.dto.NutritionEvaluationResponse;
import com.yumyumcoach.domain.aiadvisor.service.NutritionEvaluationService;
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
@RequestMapping("/api/me/ai-nutrition-evaluations")
public class NutritionEvaluationController {

    private final NutritionEvaluationService nutritionEvaluationService;

    public NutritionEvaluationController(NutritionEvaluationService nutritionEvaluationService) {
        this.nutritionEvaluationService = nutritionEvaluationService;
    }

    @PostMapping("/run")
    public NutritionEvaluationResponse runEvaluation(@RequestBody @Valid GenerateEvaluationRequest request) {
        String email = CurrentUser.email();
        return nutritionEvaluationService.evaluate(email, request.getTargetDate());
    }

    @GetMapping("/week-view")
    public NutritionEvaluationResponse getEvaluation(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = CurrentUser.email();
        return nutritionEvaluationService.getEvaluation(email, date);
    }
}
