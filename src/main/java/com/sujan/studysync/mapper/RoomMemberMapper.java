package com.sujan.studysync.mapper;

import com.sujan.studysync.dto.response.RoomMemberResponse;
import com.sujan.studysync.model.RoomMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMemberMapper {

    // RoomMember has: id, room, user, role(enum), joinedAt
    // RoomMemberResponse wants: userId, name, username, avatarUrl, role(String), isOnline, joinedAt

    // "user.id"       → userId      (nested field)
    // "user.name"     → name        (nested field)
    // "user.username" → username    (nested field)
    // "user.avatarUrl"→ avatarUrl   (nested field)
    // "user.isOnline" → isOnline    (nested field)
    // "role"          → role        (enum → String)

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "isOnline", source = "user.isOnline")
    @Mapping(target = "role",
            expression = "java(member.getRole().name())")
    RoomMemberResponse toResponse(RoomMember member);
}
