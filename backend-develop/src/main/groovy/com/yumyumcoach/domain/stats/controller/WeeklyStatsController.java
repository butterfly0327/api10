package com.yumyumcoach.domain.stats.controller;

import com.yumyumcoach.domain.stats.dto.WeeklyStatsResponse;
import com.yumyumcoach.domain.stats.service.WeeklyStatsService;
import com.yumyumcoach.global.common.CurrentUser;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/stats")
public class WeeklyStatsController {

    private final WeeklyStatsService weeklyStatsService;

    public WeeklyStatsController(WeeklyStatsService weeklyStatsService) {
        this.weeklyStatsService = weeklyStatsService;
    }

    @GetMapping("/week")
    public WeeklyStatsResponse getWeeklyStats(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String email = CurrentUser.email();
        return weeklyStatsService.getWeeklyStats(email, date);
    }
}
