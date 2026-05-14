package com.sujan.studysync.dto.response;

public record AuthResponse(
        String       accessToken,
        UserResponse user
) {}