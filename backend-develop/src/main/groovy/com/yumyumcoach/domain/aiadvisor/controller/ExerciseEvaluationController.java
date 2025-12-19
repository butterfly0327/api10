package com.yumyumcoach.domain.aiadvisor.controller;

import com.yumyumcoach.domain.aiadvisor.dto.ExerciseEvaluationResponse;
import com.yumyumcoach.domain.aiadvisor.dto.GenerateEvaluationRequest;
import com.yumyumcoach.domain.aiadvisor.service.ExerciseEvaluationService;
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
@RequestMapping("/api/me/ai-exercise-evaluations")
public class ExerciseEvaluationController {

    private final ExerciseEvaluationService exerciseEvaluationService;

    public ExerciseEvaluationController(ExerciseEvaluationService exerciseEvaluationService) {
        this.exerciseEvaluationService = exerciseEvaluationService;
    }

    @PostMapping("/run")
    public ExerciseEvaluationResponse runEvaluation(@RequestBody @Valid GenerateEvaluationRequest request) {
        String email = CurrentUser.email();
        return exerciseEvaluationService.evaluate(email, request.getTargetDate());
    }

    @GetMapping("/week-view")
    public ExerciseEvaluationResponse getEvaluation(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = CurrentUser.email();
        return exerciseEvaluationService.getEvaluation(email, date);
    }
}
