package com.sujan.studysync.dto.response;

// Broadcast to all room members so they can show the typing indicator
public record TypingResponse(
        Long    userId,
        String  username,
        String  name,
        boolean isTyping
) {}
