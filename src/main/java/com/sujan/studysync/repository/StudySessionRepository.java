package com.sujan.studysync.repository;

import com.sujan.studysync.model.StudySession;
import com.sujan.studysync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    // Find active session for user
    Optional<StudySession> findByUserAndIsActiveTrue(User user);

    // All sessions for user in date range
    List<StudySession> findByUserAndStartedAtBetweenOrderByStartedAtAsc(
            User user,
            LocalDateTime from,
            LocalDateTime to
    );

    // Total minutes this week
    @Query("""
        SELECT COALESCE(SUM(s.durationMinutes), 0)
        FROM StudySession s
        WHERE s.user = :user
        AND s.startedAt >= :from
        AND s.endedAt IS NOT NULL
        """)
    Integer sumMinutesSince(
            @Param("user") User user,
            @Param("from") LocalDateTime from
    );

    // Daily minutes for weekly chart — last 7 days
    @Query("""
        SELECT FUNCTION('DATE', s.startedAt),
               COALESCE(SUM(s.durationMinutes), 0)
        FROM StudySession s
        WHERE s.user = :user
        AND s.startedAt >= :from
        AND s.endedAt IS NOT NULL
        GROUP BY FUNCTION('DATE', s.startedAt)
        ORDER BY FUNCTION('DATE', s.startedAt) ASC
        """)
    List<Object[]> getDailyMinutes(
            @Param("user") User user,
            @Param("from") LocalDateTime from
    );

    // ─── Sum minutes in an exact date range ───────────────────────
// Used for "yesterday only" calculation
// "from" = yesterday midnight, "to" = today midnight
    @Query("""
    SELECT COALESCE(SUM(s.durationMinutes), 0)
    FROM StudySession s
    WHERE s.user = :user
    AND s.startedAt >= :from
    AND s.startedAt <  :to
    AND s.endedAt IS NOT NULL
    """)
    Integer sumMinutesBetween(
            @Param("user") User user,
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );
}