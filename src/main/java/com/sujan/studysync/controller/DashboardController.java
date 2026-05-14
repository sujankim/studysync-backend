package com.sujan.studysync.controller;

import com.sujan.studysync.dto.response.DashboardStatsResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard",
        description = "Get dashboard stats and weekly chart data")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary     = "Get dashboard stats",
            description = """
            Returns:
            - Stat cards: study time, rooms, resources, streak
            - Weekly chart data (last 7 days)
            - User info (name, plan)
            """
    )
    @GetMapping
    public ResponseEntity<DashboardStatsResponse> getStats(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                dashboardService.getStats(currentUser));
    }
}
