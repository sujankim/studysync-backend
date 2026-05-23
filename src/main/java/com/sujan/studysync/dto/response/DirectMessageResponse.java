package com.sujan.studysync.dto.response;

import java.time.LocalDateTime;

public record DirectMessageResponse(
        Long          id,
        Long          conversationId,
        UserResponse  sender,
        String        content,
        Boolean       isRead,
        LocalDateTime createdAt
) {}