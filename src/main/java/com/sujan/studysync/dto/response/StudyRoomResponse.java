package com.sujan.studysync.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record StudyRoomResponse(
        Long          id,
        String        name,
        String        slug,
        String        description,
        String        topic,
        List<String>  tags,
        String        coverImageUrl,
        Boolean       isPrivate,
        String        inviteCode,    // null unless current user is OWNER
        Integer       maxMembers,
        Integer       memberCount,
        UserResponse  owner,
        Boolean       isMember,      // is the requesting user a member?
        String        memberRole,    // "OWNER" / "MODERATOR" / "MEMBER" / null
        LocalDateTime createdAt
) {}