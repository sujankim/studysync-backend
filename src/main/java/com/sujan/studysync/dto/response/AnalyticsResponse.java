package com.sujan.studysync.dto.response;

import java.util.List;

// Full analytics data for the analytics page
// Includes overall stats + chart data + time distribution
public record AnalyticsResponse(

        // ─── Totals ──────────────────────────────────────────────
        String  totalStudyTime,      // "12h 45m"
        Integer totalSessions,       // 9
        String  avgSessionTime,      // "1h 25m"
        Integer focusScore,          // 0-100 percentage

        // ─── Change vs previous period ────────────────────────────
        Double  studyTimeChange,     // +14.0 (percent)
        Boolean studyTimeUp,
        Integer sessionsChange,      // +2
        Boolean sessionsUp,
        Double  avgSessionChange,    // +16.0
        Boolean avgSessionUp,
        Integer focusScoreChange,    // +13
        Boolean focusScoreUp,

        // ─── Weekly bar chart (study hours per day) ───────────────
        List<String>  chartDays,     // ["Mon", "Tue", ...]
        List<Double>  chartHours,    // [2.5, 1.5, 3.0, ...]

        // ─── Time distribution doughnut chart ─────────────────────
        // How study time is split: rooms vs self study
        Integer roomStudyPercent,    // 60
        Integer selfStudyPercent,    // 30
        Integer resourcePercent,     // 10 (reading/browsing resources)

        // ─── Session list (recent) ────────────────────────────────
        List<SessionSummary> recentSessions
) {
    // Nested record for session summaries in the table
    public record SessionSummary(
            String date,
            String roomName,       // null = self study
            String duration,       // "1h 30m"
            Integer minutes
    ) {}
}

