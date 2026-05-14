package com.sujan.studysync.dto.response;

import java.time.LocalDateTime;

public record RoomMemberResponse(
        Long          userId,
        String        name,
        String        username,
        String        avatarUrl,
        String        role,        // "OWNER" / "MODERATOR" / "MEMBER"
        Boolean       isOnline,
        LocalDateTime joinedAt
) {}