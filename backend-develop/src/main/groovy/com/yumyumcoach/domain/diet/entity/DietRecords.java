package com.yumyumcoach.domain.diet.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DietRecords {

    private Long id;
    private String email;
    private LocalDateTime recordDate;
    private String mealType;

    @Builder
    public DietRecords(String email, LocalDateTime recordDate, String mealType) {
        this.email = email;
        this.recordDate = recordDate;
        this.mealType = mealType;
    }
}


