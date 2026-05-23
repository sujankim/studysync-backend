package com.sujan.studysync.dto.payload;

// What Angular sends over WebSocket to send a DM
public record DirectMessagePayload(
        Long   conversationId,
        String content
) {}