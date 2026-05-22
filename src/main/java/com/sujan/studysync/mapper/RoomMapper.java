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
import java.util.Set;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface RoomMapper {

    // ─── Original method (still used internally) ──────────────
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
    @Mapping(target = "owner", source = "owner")
    StudyRoomResponse toResponse(
            StudyRoom room,
            @Context User currentUser
    );

    // overload with pre-computed memberRoomIds Set
    // This avoids lazy-loading members for every room in a list
    // memberRoomIds = Set of room IDs the user belongs to
    @Mapping(target = "tags",
            expression = "java(splitTags(room.getTags()))")
    @Mapping(target = "memberCount",
            expression = "java(room.getMemberCount())")
    @Mapping(target = "isMember",
            expression = "java(memberRoomIds.contains(room.getId()))")
    @Mapping(target = "memberRole",
            expression = "java(memberRoomIds.contains(room.getId()) ? getMemberRole(room, currentUser) : null)")
    @Mapping(target = "inviteCode",
            expression = "java(memberRoomIds.contains(room.getId()) ? getInviteCode(room, currentUser) : null)")
    @Mapping(target = "owner", source = "owner")
    StudyRoomResponse toResponse(
            StudyRoom room,
            @Context User currentUser,
            @Context Set<Long> memberRoomIds
    );

    // ─── Default helpers ──────────────────────────────────────

    default List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) return List.of();
        return Arrays.asList(tags.split(","));
    }

    // Used by original toResponse() — safe when members are loaded
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
        boolean isOwner = room.getMembers().stream()
                .anyMatch(m -> m.getUser().getId()
                        .equals(currentUser.getId())
                        && "OWNER".equals(m.getRole().name()));
        return isOwner ? room.getInviteCode() : null;
    }
}
