package com.sujan.studysync.dto.request;

// Both fields optional — can start session without a room
public record StartSessionRequest(
        Long   roomId,    // null = self study
        String roomName   // null = self study
) {}
