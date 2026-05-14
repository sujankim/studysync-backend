package com.sujan.studysync.dto.response;

import java.util.List;

public record DashboardStatsResponse(
        // ─── Stat cards ──────────────────────────────────────
        String  studyTimeToday,            // formatted: "3h 45m"
        Integer studyMinutesToday,         // raw number
        Double  studyTimeChangePercent,    // 12.5
        Boolean studyTimeUp,               // true = up from yesterday

        Integer roomsJoined,
        Integer roomsJoinedThisWeek,

        Integer resourcesShared,
        Integer resourcesThisWeek,

        Integer currentStreak,
        Integer longestStreak,
        Integer totalStudyDays,
        Integer totalStudyMinutes,

        // ─── Weekly chart ─────────────────────────────────────
        List<String>  weekDays,        // ["Mon", "Tue", ...]
        List<Integer> weeklyMinutes,   // [120, 90, 150, ...]

        // ─── User info ────────────────────────────────────────
        String userName,
        String userPlan
) {}
