package com.sujan.studysync.dto.response;

import java.time.LocalDateTime;

public record ResourceResponse(
        Long          id,
        String        title,
        String        description,
        String        type,           // "PDF", "IMAGE" etc.
        String        url,            // Cloudinary URL or link
        Long          fileSize,       // bytes — null for links
        String        originalFileName,
        Long          roomId,
        UserResponse  uploadedBy,
        LocalDateTime createdAt
) {}
