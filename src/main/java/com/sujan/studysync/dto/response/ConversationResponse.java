package com.sujan.studysync.dto.response;

import java.time.LocalDateTime;

// Sent to Angular for the conversation list
// otherUser = the OTHER person in the DM
public record ConversationResponse(
        Long          id,
        UserResponse  otherUser,
        String        lastMessage,
        LocalDateTime lastMessageAt,
        long          unreadCount,   // unread messages for current user
        LocalDateTime createdAt
) {}