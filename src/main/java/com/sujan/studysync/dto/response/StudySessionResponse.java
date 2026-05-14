package com.sujan.studysync.dto.response;

import java.time.LocalDateTime;

public record StudySessionResponse(
        Long          id,
        Long          roomId,
        String        roomName,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer       durationMinutes,
        Boolean       isActive
) {}
