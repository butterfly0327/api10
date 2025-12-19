package com.yumyumcoach.domain.diet.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietRecordDto {

    private Long id;
    private LocalDate recordDate;
    private String mealType;
    private List<DietFoodDto> items;
}


