package com.sujan.studysync.dto.response;

public record UserResponse(
        Long   id,
        String name,
        String username,
        String email,
        String avatarUrl,
        String bio,
        String role,    // "ROLE_USER" or "ROLE_ADMIN"
        String plan     // "FREE" or "PRO"
) {}
