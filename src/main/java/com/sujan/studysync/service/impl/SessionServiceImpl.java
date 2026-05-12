package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.request.StartSessionRequest;
import com.sujan.studysync.dto.response.StudySessionResponse;
import com.sujan.studysync.exception.UnauthorizedException;
import com.sujan.studysync.model.StudySession;
import com.sujan.studysync.model.User;
import com.sujan.studysync.model.UserStreak;
import com.sujan.studysync.repository.StudySessionRepository;
import com.sujan.studysync.repository.UserStreakRepository;
import com.sujan.studysync.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final StudySessionRepository sessionRepository;
    private final UserStreakRepository streakRepository;

    @Override
    @Transactional
    public StudySessionResponse startSession(
            StartSessionRequest request, User currentUser) {

        // End any existing active session first
        sessionRepository.findByUserAndIsActiveTrue(currentUser)
                .ifPresent(existing -> endSessionInternal(existing, currentUser));

        StudySession session = StudySession.builder()
                .user(currentUser)
                .roomId(request != null ? request.getRoomId() : null)
                .roomName(request != null ? request.getRoomName() : null)
                .startedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        return mapToResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public StudySessionResponse endSession(User currentUser) {
        StudySession session = sessionRepository
                .findByUserAndIsActiveTrue(currentUser)
                .orElseThrow(() -> new UnauthorizedException(
                        "No active session found"));

        return mapToResponse(endSessionInternal(session, currentUser));
    }

    @Override
    @Transactional(readOnly = true)
    public StudySessionResponse getActiveSession(User currentUser) {
        return sessionRepository.findByUserAndIsActiveTrue(currentUser)
                .map(this::mapToResponse)
                .orElse(null);
    }

    // ─── Private ──────────────────────────────────────────────
    private StudySession endSessionInternal(StudySession session, User user) {
        LocalDateTime now = LocalDateTime.now();
        int minutes = (int) ChronoUnit.MINUTES.between(
                session.getStartedAt(), now);

        session.setEndedAt(now);
        session.setDurationMinutes(Math.max(minutes, 1));
        session.setIsActive(false);
        sessionRepository.save(session);

        // Update streak
        updateStreak(user, minutes);

        return session;
    }

    private void updateStreak(User user, int minutesAdded) {
        UserStreak streak = streakRepository.findByUser(user)
                .orElseGet(() -> UserStreak.builder().user(user).build());

        LocalDate today = LocalDate.now();
        LocalDate lastStudy = streak.getLastStudyDate();

        if (lastStudy == null) {
            streak.setCurrentStreak(1);
            streak.setTotalDays(1);
        } else if (lastStudy.equals(today)) {
            // Same day — just add minutes
        } else if (lastStudy.equals(today.minusDays(1))) {
            // Consecutive day — extend streak
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streak.setTotalDays(streak.getTotalDays() + 1);
        } else {
            // Streak broken — reset
            streak.setCurrentStreak(1);
            streak.setTotalDays(streak.getTotalDays() + 1);
        }

        streak.setLastStudyDate(today);
        streak.setTotalMinutes(streak.getTotalMinutes() + minutesAdded);

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        streakRepository.save(streak);
    }

    private StudySessionResponse mapToResponse(StudySession session) {
        return StudySessionResponse.builder()
                .id(session.getId())
                .roomId(session.getRoomId())
                .roomName(session.getRoomName())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationMinutes(session.getDurationMinutes())
                .isActive(session.getIsActive())
                .build();
    }
}
