package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.response.AnalyticsResponse;
import com.sujan.studysync.dto.response.AnalyticsResponse.SessionSummary;
import com.sujan.studysync.dto.response.LeaderboardEntryResponse;
import com.sujan.studysync.dto.response.StreakResponse;
import com.sujan.studysync.dto.response.StreakResponse.Milestone;
import com.sujan.studysync.model.StudySession;
import com.sujan.studysync.model.User;
import com.sujan.studysync.model.UserStreak;
import com.sujan.studysync.repository.StudySessionRepository;
import com.sujan.studysync.repository.UserStreakRepository;
import com.sujan.studysync.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final StudySessionRepository sessionRepository;
    private final UserStreakRepository streakRepository;

    // ─── Analytics ────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(User currentUser, String period) {

        // Determine date range based on period
        // "weekly"  = last 7 days
        // "monthly" = last 30 days
        boolean isWeekly = !"monthly".equalsIgnoreCase(period);
        int days = isWeekly ? 7 : 30;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = now.minusDays(days);
        LocalDateTime prevStart = periodStart.minusDays(days);

        // ─── Current period stats ──────────────────────────────

        // All completed sessions in the current period
        List<StudySession> sessions =
                sessionRepository.findByUserAndStartedAtBetweenOrderByStartedAtAsc(
                        currentUser, periodStart, now);

        int totalMinutes = sessions.stream()
                .filter(s ->
                        s.getDurationMinutes() != null)
                .mapToInt(StudySession::getDurationMinutes).sum();

        int totalSessions = sessions.size();

        // Average session time (avoid division by zero)
        int avgMinutes = totalSessions > 0 ? totalMinutes / totalSessions : 0;

        // Focus score: percentage of sessions that lasted 25+ minutes
        // (Pomodoro-inspired — a "focused" session is at least 25 min)
        long focusedSessions = sessions.stream()
                .filter(s ->
                        s.getDurationMinutes() != null && s.getDurationMinutes() >= 25).count();

        int focusScore = totalSessions > 0 ? (int) ((double) focusedSessions / totalSessions * 100) : 0;

        // ─── Previous period stats (for % change) ─────────────

        List<StudySession> prevSessions = sessionRepository.findByUserAndStartedAtBetweenOrderByStartedAtAsc(currentUser, prevStart, periodStart);

        int prevMinutes = prevSessions.stream().filter(s -> s.getDurationMinutes() != null).mapToInt(StudySession::getDurationMinutes).sum();
        int prevCount = prevSessions.size();
        int prevAvg = prevCount > 0 ? prevMinutes / prevCount : 0;

        long prevFocused = prevSessions.stream().filter(s -> s.getDurationMinutes() != null && s.getDurationMinutes() >= 25).count();
        int prevFocusScore = prevCount > 0 ? (int) ((double) prevFocused / prevCount * 100) : 0;

        // ─── Chart data (hours per day) ────────────────────────

        // Build a map of date → minutes (pre-filled with 0s)
        Map<String, Integer> dailyMap = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            dailyMap.put(LocalDate.now().minusDays(i).toString(), 0);
        }

        // Fill in actual session data
        sessions.forEach(s -> {
            if (s.getDurationMinutes() == null) return;
            String key = s.getStartedAt().toLocalDate().toString();
            dailyMap.merge(key, s.getDurationMinutes(), Integer::sum);
        });

        // Convert to labels + hours (not minutes) for the chart
        List<String> chartDays = new ArrayList<>();
        List<Double> chartHours = new ArrayList<>();

        dailyMap.forEach((dateStr, mins) -> {
            LocalDate date = LocalDate.parse(dateStr);
            // Weekly → "Mon", Monthly → "May 10"
            String label = isWeekly ? date.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) : date.getMonthValue() + "/" + date.getDayOfMonth();

            chartDays.add(label);
            // Round to 1 decimal place for chart readability
            chartHours.add(Math.round(mins / 60.0 * 10.0) / 10.0);
        });

        // ─── Time distribution ─────────────────────────────────

        // Sessions with a room = "room study"
        long roomMins = sessions.stream()
                .filter(s ->
                        s.getRoomId() != null && s.getDurationMinutes() != null)
                .mapToInt(StudySession::getDurationMinutes).sum();

        // Sessions without a room = "self study"
        long selfMins = totalMinutes - roomMins;

        int roomPct = totalMinutes > 0 ? (int) (roomMins * 100L / totalMinutes) : 60;
        int selfPct = totalMinutes > 0 ? (int) (selfMins * 100L / totalMinutes) : 30;
        // Resources = whatever is left (keeps total = 100%)
        int resourcePct = 100 - roomPct - selfPct;

        // ─── Recent sessions list ──────────────────────────────

        List<AnalyticsResponse.SessionSummary> recentSessions =
                sessions.stream()
                        .filter(s ->
                                s.getDurationMinutes() != null)
                        .sorted(Comparator
                                .comparing(StudySession::getStartedAt)
                                .reversed()).limit(10).map(s ->
                                new AnalyticsResponse.SessionSummary(s.getStartedAt()
                                        .toLocalDate().toString(), s.getRoomName() != null
                                        ? s.getRoomName() : "Self Study",
                                        formatMinutes(s.getDurationMinutes()),
                                        s.getDurationMinutes())).toList();

        // ─── Build response ────────────────────────────────────
        return new AnalyticsResponse(formatMinutes(totalMinutes), totalSessions, formatMinutes(avgMinutes), focusScore,

                calcPercent(totalMinutes, prevMinutes), totalMinutes >= prevMinutes, totalSessions - prevCount, totalSessions >= prevCount, calcPercent(avgMinutes, prevAvg), avgMinutes >= prevAvg, focusScore - prevFocusScore, focusScore >= prevFocusScore,

                chartDays, chartHours,

                roomPct, selfPct, resourcePct,

                recentSessions);
    }

    // ─── Streak ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public StreakResponse getStreak(User currentUser) {

        UserStreak streak = streakRepository.findByUser(currentUser).orElseGet(() -> UserStreak.builder().user(currentUser).build());

        // Last 7 days activity — did the user study on each day?
        List<Boolean> last7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            // sumMinutesBetween returns 0 if no sessions that day
            Integer mins = sessionRepository.sumMinutesBetween(currentUser, start, end);
            last7Days.add(mins != null && mins > 0);
        }

        // Streak milestones
        List<StreakResponse.Milestone> milestones = List.of(new StreakResponse.Milestone(3, "3 days", "🔥", streak.getCurrentStreak() >= 3), new StreakResponse.Milestone(7, "One week", "⚡", streak.getCurrentStreak() >= 7), new StreakResponse.Milestone(14, "Two weeks", "💪", streak.getCurrentStreak() >= 14), new Milestone(30, "One month", "💎", streak.getCurrentStreak() >= 30), new Milestone(100, "Legend", "🏆", streak.getCurrentStreak() >= 100));

        return new StreakResponse(streak.getCurrentStreak(), streak.getLongestStreak(), streak.getTotalDays(), streak.getTotalMinutes(), streak.getLastStudyDate(), last7Days, milestones);
    }

    // ─── Leaderboard ──────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(User currentUser, String period) {

        // Determine time range
        LocalDateTime from = switch (period.toLowerCase()) {
            case "month" -> LocalDateTime.now().minusDays(30);
            case "all" -> LocalDateTime.of(2020, 1, 1, 0, 0);
            default -> LocalDateTime.now().minusDays(7); // "week"
        };

        // Top 20 users
        List<Object[]> rows = sessionRepository.getLeaderboard(from, PageRequest.of(0, 20));

        List<LeaderboardEntryResponse> entries = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            Long userId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String username = (String) row[2];
            String avatarUrl = (String) row[3];
            int minutes = ((Number) row[4]).intValue();

            entries.add(new LeaderboardEntryResponse(i + 1,                             // rank (1-based)
                    userId, name, username, avatarUrl, minutes, userId.equals(currentUser.getId()) // is this the current user?
            ));
        }

        return entries;
    }

    // ─── Private helpers ──────────────────────────────────────

    private String formatMinutes(int minutes) {
        if (minutes == 0) return "0m";
        int h = minutes / 60;
        int m = minutes % 60;
        if (h == 0) return m + "m";
        if (m == 0) return h + "h";
        return h + "h " + m + "m";
    }

    // Calculate percentage change between current and previous value
    // Returns 0.0 if previous is 0 (avoid division by zero)
    private double calcPercent(int current, int previous) {
        if (previous == 0) return 0.0;
        double raw = ((double) (current - previous) / previous) * 100.0;
        // Round to 1 decimal
        return Math.round(raw * 10.0) / 10.0;
    }
}
