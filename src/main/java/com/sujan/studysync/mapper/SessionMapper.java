package com.sujan.studysync.mapper;

import com.sujan.studysync.dto.response.StudySessionResponse;
import com.sujan.studysync.model.StudySession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    // StudySession fields match StudySessionResponse record exactly:
    // id, roomId, roomName, startedAt, endedAt, durationMinutes, isActive
    // MapStruct handles all of these automatically — no @Mapping needed!
    StudySessionResponse toResponse(StudySession session);
}
