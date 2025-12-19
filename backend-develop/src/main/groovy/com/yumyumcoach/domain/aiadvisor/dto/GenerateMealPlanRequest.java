package com.yumyumcoach.domain.aiadvisor.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class GenerateMealPlanRequest {
    @NotNull
    private LocalDate targetDate;
}
