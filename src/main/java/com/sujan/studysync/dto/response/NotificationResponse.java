package com.sujan.studysync.dto.response;

import java.time.LocalDateTime;

// Sent to Angular — both via REST and WebSocket push
public record NotificationResponse(
        Long          id,
        String        type,         // "ROOM_JOINED", "SYSTEM" etc.
        String        title,
        String        message,
        Boolean       isRead,
        Long          referenceId,
        String        actionUrl,
        LocalDateTime createdAt
) {}