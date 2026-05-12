package com.sujan.studysync.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardStatsResponse {

    // Stat cards
    private String  studyTimeToday;        // "3h 45m"
    private Integer studyMinutesToday;
    private Double  studyTimeChangePercent; // +12% from yesterday
    private Boolean studyTimeUp;

    private Integer roomsJoined;
    private Integer roomsJoinedThisWeek;

    private Integer resourcesShared;
    private Integer resourcesThisWeek;

    private Integer currentStreak;
    private Integer longestStreak;
    private Integer totalStudyDays;
    private Integer totalStudyMinutes;

    // Weekly chart (7 days)
    private List<String>  weekDays;         // ["Mon", "Tue", ...]
    private List<Integer> weeklyMinutes;    // [120, 90, 150, ...]

    // User info
    private String userName;
    private String userPlan;
}
