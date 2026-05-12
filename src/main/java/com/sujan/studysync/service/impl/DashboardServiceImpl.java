package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.response.DashboardStatsResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.model.UserStreak;
import com.sujan.studysync.repository.StudySessionRepository;
import com.sujan.studysync.repository.UserStreakRepository;
import com.sujan.studysync.service.DashboardService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final StudySessionRepository sessionRepository;
    private final UserStreakRepository streakRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats(User currentUser) {

        // ─── Streak data ─────────────────────────────────────
        UserStreak streak = streakRepository.findByUser(currentUser)
                .orElseGet(() -> UserStreak.builder()
                        .user(currentUser)
                        .build());

        // ─── Study time today ─────────────────────────────────
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd   = todayStart.plusDays(1);

        Integer minutesToday = sessionRepository.sumMinutesSince(
                currentUser, todayStart);

        // ─── Study time yesterday (for change %) ──────────────
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        Integer minutesYesterday = sessionRepository.sumMinutesSince(
                currentUser, yesterdayStart);

        double changePercent = 0;
        boolean up = true;
        if (minutesYesterday != null && minutesYesterday > 0 && minutesToday != null) {
            changePercent = ((double)(minutesToday - minutesYesterday) / minutesYesterday) * 100;
            up = changePercent >= 0;
        }

        // ─── Weekly chart data (last 7 days) ──────────────────
        LocalDateTime weekStart = todayStart.minusDays(6);
        List<Object[]> dailyData = sessionRepository.getDailyMinutes(
                currentUser, weekStart);

        // Build map: dateStr -> minutes
        Map<String, Integer> dailyMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            dailyMap.put(day.toString(), 0);
        }
        for (Object[] row : dailyData) {
            String dateKey = row[0].toString().substring(0, 10);
            Number minutes = (Number) row[1];
            if (dailyMap.containsKey(dateKey)) {
                dailyMap.put(dateKey, minutes.intValue());
            }
        }

        // Day labels — "Mon", "Tue", etc.
        List<String>  weekDays      = new ArrayList<>();
        List<Integer> weeklyMinutes = new ArrayList<>();

        dailyMap.forEach((dateStr, mins) -> {
            LocalDate date = LocalDate.parse(dateStr);
            String label = date.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            weekDays.add(label);
            weeklyMinutes.add(mins);
        });

        // ─── Build response ───────────────────────────────────
        return DashboardStatsResponse.builder()
                .studyTimeToday(formatMinutes(minutesToday))
                .studyMinutesToday(minutesToday)
                .studyTimeChangePercent(Math.abs(changePercent))
                .studyTimeUp(up)
                .roomsJoined(streak.getRoomsJoined())
                .roomsJoinedThisWeek(0)       // TODO: calculate from room_members
                .resourcesShared(streak.getResourcesShared())
                .resourcesThisWeek(0)         // TODO: calculate from resources
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .totalStudyDays(streak.getTotalDays())
                .totalStudyMinutes(streak.getTotalMinutes())
                .weekDays(weekDays)
                .weeklyMinutes(weeklyMinutes)
                .userName(currentUser.getName())
                .userPlan(currentUser.getPlan().name())
                .build();
    }

    // ─── Helper ───────────────────────────────────────────────
    private String formatMinutes(Integer minutes) {
        if (minutes == null || minutes == 0) return "0m";
        int h = minutes / 60;
        int m = minutes % 60;
        if (h == 0) return m + "m";
        if (m == 0) return h + "h";
        return h + "h " + m + "m";
    }
}