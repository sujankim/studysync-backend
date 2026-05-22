package com.sujan.studysync.dto.response;

// One row in the leaderboard table
public record LeaderboardEntryResponse(
        Integer rank,           // 1, 2, 3 ...
        Long    userId,
        String  name,
        String  username,
        String  avatarUrl,
        Integer totalMinutes,   // total study minutes this period
        Boolean isCurrentUser   // highlight the current user's row
) {}