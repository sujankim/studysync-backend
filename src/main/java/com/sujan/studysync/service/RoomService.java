package com.sujan.studysync.service;

import com.sujan.studysync.dto.request.CreateRoomRequest;
import com.sujan.studysync.dto.response.PageResponse;
import com.sujan.studysync.dto.response.RoomMemberResponse;
import com.sujan.studysync.dto.response.StudyRoomResponse;
import com.sujan.studysync.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomService {

    StudyRoomResponse createRoom(CreateRoomRequest request, User currentUser);

    PageResponse<StudyRoomResponse> browseRooms(
            String topic, String search,
            Pageable pageable, User currentUser);

    StudyRoomResponse getRoomById(Long roomId, User currentUser);

    List<StudyRoomResponse> getMyRooms(User currentUser);

    StudyRoomResponse joinRoom(Long roomId, User currentUser);

    StudyRoomResponse joinByInviteCode(String inviteCode, User currentUser);

    void leaveRoom(Long roomId, User currentUser);

    void deleteRoom(Long roomId, User currentUser);

    List<RoomMemberResponse> getRoomMembers(Long roomId);
}
