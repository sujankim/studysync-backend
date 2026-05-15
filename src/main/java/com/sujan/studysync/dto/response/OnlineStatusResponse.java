package com.sujan.studysync.dto.response;

// Sent when a user connects or disconnects from a room
// So all room members know who is currently online
public record OnlineStatusResponse(
        Long    userId,
        String  username,
        String  name,
        String  avatarUrl,
        boolean isOnline
) {}
