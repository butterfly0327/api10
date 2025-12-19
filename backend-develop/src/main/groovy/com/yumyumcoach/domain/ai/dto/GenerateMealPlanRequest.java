package com.yumyumcoach.domain.ai.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenerateMealPlanRequest {
    private LocalDate date;
}
