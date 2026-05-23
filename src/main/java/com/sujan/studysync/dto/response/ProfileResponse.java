package com.sujan.studysync.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Full profile — richer than UserResponse
// Includes streak + room + resource stats
public record ProfileResponse(
        Long          id,
        String        name,
        String        username,
        String        email,
        String        avatarUrl,
        String        bio,
        String        role,
        String        plan,
        String        provider,      // "local" or "google"

        // Streak stats
        Integer       currentStreak,
        Integer       longestStreak,
        Integer       totalStudyDays,
        Integer       totalStudyMinutes,
        LocalDate     lastStudyDate,

        // Room stats
        Integer       roomsJoined,
        Integer       resourcesShared,

        LocalDateTime memberSince
) {}
