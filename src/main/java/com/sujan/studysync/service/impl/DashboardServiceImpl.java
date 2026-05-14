package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.response.DashboardStatsResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.model.UserStreak;
import com.sujan.studysync.repository.StudySessionRepository;
import com.sujan.studysync.repository.UserStreakRepository;
import com.sujan.studysync.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudySessionRepository sessionRepository;
    private final UserStreakRepository   streakRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(User currentUser) {

        // ─── 1. Streak ────────────────────────────────────────
        // Try to find existing streak record
        // If user has never studied → create an empty in-memory one
        UserStreak streak = streakRepository
                .findByUser(currentUser)
                .orElseGet(() -> UserStreak.builder()
                        .user(currentUser)
                        .build());

        // ─── 2. Today's study time ────────────────────────────
        // "Today" = from midnight (00:00) to now
        LocalDateTime todayStart     = LocalDate.now().atStartOfDay();

        // "Yesterday" = exactly one 24-hour window before today
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        // yesterdayEnd = todayStart
        // So: yesterday = [todayStart - 24h  →  todayStart]
        //     today      = [todayStart        →  now]

        Integer minutesToday     = sessionRepository
                .sumMinutesSince(currentUser, todayStart);

        //  pass BOTH from and to for yesterday
        // Previously sumMinutesSince only had "from" which counted
        // all sessions from yesterdayStart up to NOW (wrong!)
        // We want ONLY yesterday's sessions
        Integer minutesYesterday = sessionRepository
                .sumMinutesBetween(currentUser, yesterdayStart, todayStart);

        // ─── 3. Change % vs yesterday ─────────────────────────
        double  changePercent = 0.0;
        boolean isUp          = true;

        if (minutesYesterday != null
                && minutesYesterday > 0
                && minutesToday    != null) {

            changePercent =
                    ((double)(minutesToday - minutesYesterday)
                            / minutesYesterday) * 100.0;

            isUp = changePercent >= 0;
        }

        // ─── 4. Weekly chart data (last 7 days) ───────────────
        // Step A: Pre-fill map with last 7 days all set to 0
        // LinkedHashMap keeps insertion order (Mon → Tue → ... → Sun)
        Map<String, Integer> dailyMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            dailyMap.put(day.toString(), 0);
        }

        // Step B: Fill in real data from DB where available
        // getDailyMinutes returns rows: [date, totalMinutes]
        LocalDateTime weekStart = todayStart.minusDays(6);
        sessionRepository
                .getDailyMinutes(currentUser, weekStart)
                .forEach(row -> {
                    // row[0] might be "2026-05-10" or "2026-05-10T00:00:00"
                    // take first 10 chars to always get "yyyy-MM-dd"
                    String dateKey = row[0].toString().substring(0, 10);
                    Number minutes = (Number) row[1];
                    if (dailyMap.containsKey(dateKey)) {
                        dailyMap.put(dateKey, minutes.intValue());
                    }
                });

        // Step C: Split map into two parallel lists for the chart
        List<String>  weekDays      = new ArrayList<>();
        List<Integer> weeklyMinutes = new ArrayList<>();

        dailyMap.forEach((dateStr, mins) -> {
            LocalDate date  = LocalDate.parse(dateStr);
            String    label = date.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            weekDays.add(label);
            weeklyMinutes.add(mins);
        });

        // ─── 5. Build response ────────────────────────────────
        // Record constructor — no builder needed
        // Order must match the record definition exactly:
        // record DashboardStatsResponse(
        //   String studyTimeToday, Integer studyMinutesToday, ...
        // )
        return new DashboardStatsResponse(
                formatMinutes(minutesToday),       // studyTimeToday
                minutesToday  != null
                        ? minutesToday : 0,         // studyMinutesToday
                Math.abs(changePercent),            // studyTimeChangePercent
                isUp,                               // studyTimeUp

                streak.getRoomsJoined(),            // roomsJoined
                0,                                  // roomsJoinedThisWeek  TODO: Phase 5
                streak.getResourcesShared(),        // resourcesShared
                0,                                  // resourcesThisWeek    TODO: Phase 7

                streak.getCurrentStreak(),          // currentStreak
                streak.getLongestStreak(),          // longestStreak
                streak.getTotalDays(),              // totalStudyDays
                streak.getTotalMinutes(),           // totalStudyMinutes

                weekDays,                           // weekDays
                weeklyMinutes,                      // weeklyMinutes

                currentUser.getName(),              // userName
                currentUser.getPlan().name()        // userPlan
        );
    }

    // ─── Helper ───────────────────────────────────────────────
    // Converts raw minutes to human-readable string
    // 0       → "0m"
    // 45      → "45m"
    // 60      → "1h"
    // 225     → "3h 45m"
    private String formatMinutes(Integer minutes) {
        if (minutes == null || minutes == 0) return "0m";
        int h = minutes / 60;
        int m = minutes % 60;
        if (h == 0) return m + "m";
        if (m == 0) return h + "h";
        return h + "h " + m + "m";
    }
}