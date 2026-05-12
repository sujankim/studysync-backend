package com.sujan.studysync.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudySessionResponse {
    private Long          id;
    private Long          roomId;
    private String        roomName;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer       durationMinutes;
    private Boolean       isActive;
}
