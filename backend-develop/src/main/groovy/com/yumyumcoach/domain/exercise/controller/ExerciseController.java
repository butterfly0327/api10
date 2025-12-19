package com.yumyumcoach.domain.exercise.controller;

import com.yumyumcoach.domain.exercise.dto.ExerciseResponse;
import com.yumyumcoach.domain.exercise.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;

    @GetMapping
    public List<ExerciseResponse> getExercises() {
        return exerciseService.getExercises();
    }
}
