package com.sujan.studysync.controller;

import com.sujan.studysync.dto.response.DashboardStatsResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardStatsResponse> getStats(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(dashboardService.getStats(currentUser));
    }
}
