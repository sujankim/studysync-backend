package com.sujan.studysync.service;

import com.sujan.studysync.dto.response.AnalyticsResponse;
import com.sujan.studysync.dto.response.LeaderboardEntryResponse;
import com.sujan.studysync.dto.response.StreakResponse;
import com.sujan.studysync.model.User;

import java.util.List;

public interface AnalyticsService {

    // Full analytics for the analytics page
    // period = "weekly" | "monthly"
    AnalyticsResponse getAnalytics(User currentUser, String period);

    // Streak data with milestones
    StreakResponse getStreak(User currentUser);

    // Leaderboard
    // period = "week" | "month" | "all"
    List<LeaderboardEntryResponse> getLeaderboard(
            User currentUser, String period);
}
