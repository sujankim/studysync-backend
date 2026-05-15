package com.sujan.studysync.dto.response;

import java.time.LocalDateTime;

// Sent to client when a message is created or retrieved from history
// Note: record fields are ordered — constructor must match exactly
public record MessageResponse(
        Long          id,
        String        content,
        Long          roomId,
        UserResponse  sender,      // nested — who sent it
        Boolean       isEdited,
        LocalDateTime createdAt
) {}
