package com.sujan.studysync.service;

import com.sujan.studysync.dto.response.DashboardStatsResponse;
import com.sujan.studysync.model.User;

public interface DashboardService {
    DashboardStatsResponse getStats(User currentUser);
}
