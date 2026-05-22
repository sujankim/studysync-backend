package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.request.CreateRoomRequest;
import com.sujan.studysync.dto.response.PageResponse;
import com.sujan.studysync.dto.response.RoomMemberResponse;
import com.sujan.studysync.dto.response.StudyRoomResponse;
import com.sujan.studysync.dto.response.UserResponse;
import com.sujan.studysync.enums.RoomRole;
import com.sujan.studysync.exception.*;
import com.sujan.studysync.mapper.RoomMapper;
import com.sujan.studysync.mapper.RoomMemberMapper;
import com.sujan.studysync.model.RoomMember;
import com.sujan.studysync.model.StudyRoom;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.RoomMemberRepository;
import com.sujan.studysync.repository.StudyRoomRepository;
import com.sujan.studysync.service.RoomService;
import com.sujan.studysync.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final StudyRoomRepository roomRepository;
    private final RoomMemberRepository memberRepository;
    private final SlugUtil slugUtil;
    private final RoomMapper roomMapper;
    private final RoomMemberMapper roomMemberMapper;

    // ─── Create ───────────────────────────────────────────────
    @Override
    @Transactional
    public StudyRoomResponse createRoom(
            CreateRoomRequest request, User currentUser) {

        StudyRoom room = StudyRoom.builder()
                .name(request.name())
                .slug(slugUtil.generateSlug(request.name()))
                .description(request.description())
                .topic(request.topic())
                .tags(tagsToString(request.tags()))
                .isPrivate(Boolean.TRUE.equals(request.isPrivate()))
                .maxMembers(request.maxMembers() != null
                        ? request.maxMembers() : 50)
                .owner(currentUser)
                .inviteCode(Boolean.TRUE.equals(request.isPrivate())
                        ? slugUtil.generateInviteCode() : null)
                .build();

        RoomMember ownerMember = RoomMember.builder()
                .room(room)
                .user(currentUser)
                .role(RoomRole.OWNER)
                .build();

        room.getMembers().add(ownerMember);
        roomRepository.save(room);

        // Reload from DB — ensures members collection is fresh
        // After save(), Hibernate replaces in-memory list with proxy
        // The proxy correctly loads from DB and isMember() works
        StudyRoom savedRoom = roomRepository
                .findById(room.getId())
                .orElse(room);

        // Creator is always a member
        Set<Long> memberRoomIds = Set.of(savedRoom.getId());

        return roomMapper.toResponse(savedRoom, currentUser, memberRoomIds);
    }

    // ─── Browse ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudyRoomResponse> browseRooms(
            String topic, String search,
            Pageable pageable, User currentUser) {

        Page<StudyRoom> page;

        if (StringUtils.hasText(search)) {
            page = roomRepository.searchPublic(search, pageable);
        } else if (StringUtils.hasText(topic)
                && !"All Topics".equalsIgnoreCase(topic)) {
            page = roomRepository
                    .findByIsPrivateFalseAndTopicIgnoreCaseOrderByCreatedAtDesc(
                            topic, pageable);
        } else {
            page = roomRepository
                    .findByIsPrivateFalseOrderByCreatedAtDesc(pageable);
        }

        // ONE query to get all room IDs where user is a member
        // Much faster than lazy-loading members for each room
        Set<Long> memberRoomIds = roomRepository
                .findRoomIdsByMember(currentUser);

        // Pass to mapper as context — avoids lazy collection access
        List<StudyRoomResponse> content = page.getContent()
                .stream()
                .map(room -> roomMapper.toResponse(
                        room, currentUser, memberRoomIds))
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    // ─── Get Single Room ──────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public StudyRoomResponse getRoomById(Long roomId, User currentUser) {
        StudyRoom room = findOrThrow(roomId);

        // Check membership with a single DB query
        boolean isMember = memberRepository
                .existsByRoomAndUser(room, currentUser);

        Set<Long> memberRoomIds = isMember
                ? Set.of(roomId)
                : Set.of();

        return roomMapper.toResponse(room, currentUser, memberRoomIds);
    }

    // ─── My Rooms ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<StudyRoomResponse> getMyRooms(User currentUser) {
        // My rooms → user IS always a member → pass as known set
        List<StudyRoom> rooms = roomRepository.findByMember(currentUser);

        // All these rooms have currentUser as member
        Set<Long> memberRoomIds = rooms.stream()
                .map(StudyRoom::getId)
                .collect(java.util.stream.Collectors.toSet());

        return rooms.stream()
                .map(room -> roomMapper.toResponse(
                        room, currentUser, memberRoomIds))
                .toList();
    }

    // ─── Join ─────────────────────────────────────────────────
    @Override
    @Transactional
    public StudyRoomResponse joinRoom(Long roomId, User currentUser) {
        StudyRoom room = findOrThrow(roomId);

        if (memberRepository.existsByRoomAndUser(room, currentUser)) {
            throw new AlreadyMemberException();
        }
        if (room.getMemberCount() >= room.getMaxMembers()) {
            throw new RoomFullException();
        }

        RoomMember member = RoomMember.builder()
                .room(room)
                .user(currentUser)
                .role(RoomRole.MEMBER)
                .build();

        memberRepository.save(member);
        room.getMembers().add(member);

        // User just joined — they are definitely a member
        return roomMapper.toResponse(
                room, currentUser, Set.of(roomId));
    }

    // ─── Join by Invite Code ──────────────────────────────────
    @Override
    @Transactional
    public StudyRoomResponse joinByInviteCode(
            String inviteCode, User currentUser) {

        StudyRoom room = roomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RoomNotFoundException(
                        "Invalid invite code: " + inviteCode));

        return joinRoom(room.getId(), currentUser);
    }

    // ─── Leave ────────────────────────────────────────────────
    @Override
    @Transactional
    public void leaveRoom(Long roomId, User currentUser) {
        StudyRoom room = findOrThrow(roomId);

        RoomMember member = memberRepository
                .findByRoomAndUser(room, currentUser)
                .orElseThrow(NotMemberException::new);

        if (member.getRole() == RoomRole.OWNER) {
            throw new UnauthorizedException(
                    "Room owner cannot leave. Delete the room instead.");
        }

        memberRepository.deleteByRoomAndUser(room, currentUser);
    }

    // ─── Delete ───────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteRoom(Long roomId, User currentUser) {
        StudyRoom room = findOrThrow(roomId);

        RoomMember member = memberRepository
                .findByRoomAndUser(room, currentUser)
                .orElseThrow(NotMemberException::new);

        if (member.getRole() != RoomRole.OWNER) {
            throw new UnauthorizedException(
                    "Only the room owner can delete this room.");
        }

        roomRepository.delete(room);
    }

    // ─── Members ──────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<RoomMemberResponse> getRoomMembers(Long roomId) {
        return findOrThrow(roomId).getMembers().stream()
                .map(roomMemberMapper::toResponse)
                .toList();
    }

    // ─── Private Helpers ──────────────────────────────────────

    private StudyRoom findOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    private String tagsToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return String.join(",", tags);
    }
}
