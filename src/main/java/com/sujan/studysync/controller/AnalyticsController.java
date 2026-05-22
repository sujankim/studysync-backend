package com.sujan.studysync.controller;

import com.sujan.studysync.dto.response.AnalyticsResponse;
import com.sujan.studysync.dto.response.LeaderboardEntryResponse;
import com.sujan.studysync.dto.response.StreakResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Analytics",
        description = "Study analytics, streaks, and leaderboard")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // GET /api/analytics?period=weekly
    // GET /api/analytics?period=monthly
    @Operation(
            summary = "Get study analytics",
            description = "Returns charts, totals, and session history. " +
                    "period = 'weekly' (default) or 'monthly'"
    )
    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @Parameter(description = "weekly or monthly")
            @RequestParam(defaultValue = "weekly") String period,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                analyticsService.getAnalytics(currentUser, period));
    }

    // GET /api/analytics/streak
    @Operation(summary = "Get streak data and milestones")
    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getStreak(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                analyticsService.getStreak(currentUser));
    }

    // GET /api/analytics/leaderboard?period=week
    // GET /api/analytics/leaderboard?period=month
    // GET /api/analytics/leaderboard?period=all
    @Operation(
            summary = "Get leaderboard",
            description = "Top 20 users by study time. " +
                    "period = 'week' | 'month' | 'all'"
    )
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(
            @Parameter(description = "week, month, or all")
            @RequestParam(defaultValue = "week") String period,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                analyticsService.getLeaderboard(currentUser, period));
    }
}
