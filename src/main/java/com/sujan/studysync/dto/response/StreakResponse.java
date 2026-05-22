package com.sujan.studysync.dto.response;

import java.time.LocalDate;
import java.util.List;

// Full streak data for the streak page
public record StreakResponse(
        Integer       currentStreak,
        Integer       longestStreak,
        Integer       totalDays,
        Integer       totalMinutes,
        LocalDate     lastStudyDate,

        // Activity heatmap — last 7 days true/false
        List<Boolean> last7Days,

        // Milestone thresholds + whether achieved
        List<Milestone> milestones
) {
    // Nested record — each streak milestone
    // e.g. 3-day streak, 7-day streak, 30-day streak
    public record Milestone(
            Integer days,        // 3, 7, 14, 30, 100
            String  label,       // "3 days", "One week"
            String  emoji,       // "🔥", "⚡", "💎"
            Boolean achieved     // has the user hit this milestone?
    ) {}
}

