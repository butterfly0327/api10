package com.yumyumcoach.domain.aiadvisor.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class GenerateEvaluationRequest {
    @NotNull
    private LocalDate targetDate;
}
