package com.sujan.studysync.mapper;

// ⚠️ RoomMapper is MORE COMPLEX than others
// Because StudyRoomResponse needs:
//   - tags: List<String>   (stored as comma string in DB)
//   - isMember: Boolean    (depends on WHICH user is requesting)
//   - memberRole: String   (depends on WHICH user is requesting)
//   - inviteCode: String   (only shown to OWNER)
//
// MapStruct can't know "which user" at compile time
// So we use @Context to pass the current user at runtime
// and @Mapping(expression=...) for custom logic

import com.sujan.studysync.dto.response.StudyRoomResponse;
import com.sujan.studysync.model.StudyRoom;
import com.sujan.studysync.model.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
// "uses = {UserMapper.class}" means:
// "When you need to map a User to UserResponse inside this mapper,
//  use UserMapper.toResponse() — don't try to figure it out yourself"
public interface RoomMapper {

    @Mapping(target = "tags",
            expression = "java(splitTags(room.getTags()))")

    @Mapping(target = "memberCount",
            expression = "java(room.getMemberCount())")

    @Mapping(target = "isMember",
            expression = "java(isMember(room, currentUser))")

    @Mapping(target = "memberRole",
            expression = "java(getMemberRole(room, currentUser))")

    @Mapping(target = "inviteCode",
            expression = "java(getInviteCode(room, currentUser))")

    @Mapping(target = "owner",
            source = "owner")
    // "source = owner" → MapStruct uses UserMapper.toResponse(room.getOwner())
    // because we declared "uses = {UserMapper.class}" above

    StudyRoomResponse toResponse(
            StudyRoom room,
            @Context User currentUser   // ← passed at call time, not mapped
    );

    // ─── Default helper methods ──────────────────────────────
    // MapStruct calls these when it sees the expressions above

    default List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) return List.of();
        return Arrays.asList(tags.split(","));
    }

    default boolean isMember(StudyRoom room, User currentUser) {
        return room.getMembers().stream()
                .anyMatch(m -> m.getUser().getId()
                        .equals(currentUser.getId()));
    }

    default String getMemberRole(StudyRoom room, User currentUser) {
        return room.getMembers().stream()
                .filter(m -> m.getUser().getId()
                        .equals(currentUser.getId()))
                .findFirst()
                .map(m -> m.getRole().name())
                .orElse(null);
    }

    default String getInviteCode(StudyRoom room, User currentUser) {
        // Only show invite code to the OWNER of the room
        boolean isOwner = room.getMembers().stream()
                .anyMatch(m -> m.getUser().getId()
                        .equals(currentUser.getId())
                        && m.getRole().name().equals("OWNER"));
        return isOwner ? room.getInviteCode() : null;
    }
}
