package com.sujan.studysync.controller;

import com.sujan.studysync.dto.request.CreateRoomRequest;
import com.sujan.studysync.dto.response.PageResponse;
import com.sujan.studysync.dto.response.RoomMemberResponse;
import com.sujan.studysync.dto.response.StudyRoomResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @SecurityRequirement("Bearer Authentication") means:
// "All endpoints in this controller require the JWT token"
// This makes the padlock icon appear on each endpoint in Swagger UI
@Tag(name = "Study Rooms",
        description = "Create, browse, join, leave, delete study rooms")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // POST /api/rooms
    @Operation(summary = "Create a new study room")
    @ApiResponse(responseCode = "201", description = "Room created successfully")
    @PostMapping
    public ResponseEntity<StudyRoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roomService.createRoom(request, currentUser));
    }


    // GET /api/rooms?topic=Java&search=spring&page=0&size=12
    @Operation(
            summary = "Browse public study rooms",
            description = "Returns paginated list. Filter by topic or search by name/description."
    )
    @GetMapping
    public ResponseEntity<PageResponse<StudyRoomResponse>> browseRooms(
            @Parameter(description = "Filter by topic e.g. Java, DSA")
            @RequestParam(required = false) String topic,

            @Parameter(description = "Search in name, description, topic")
            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size,

            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(roomService.browseRooms(
                topic, search,
                PageRequest.of(page, size),
                currentUser));
    }

    // GET /api/rooms/my
    @Operation(summary = "Get rooms you have joined")
    @GetMapping("/my")
    public ResponseEntity<List<StudyRoomResponse>> getMyRooms(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(roomService.getMyRooms(currentUser));
    }

    // GET /api/rooms/{roomId}
    @Operation(summary = "Get a room by its ID")
    @GetMapping("/{roomId}")
    public ResponseEntity<StudyRoomResponse> getRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                roomService.getRoomById(roomId, currentUser));
    }

    // POST /api/rooms/{roomId}/join
    @Operation(summary = "Join a public room")
    @PostMapping("/{roomId}/join")
    public ResponseEntity<StudyRoomResponse> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                roomService.joinRoom(roomId, currentUser));
    }

    // POST /api/rooms/join/invite/{code}
    @Operation(summary = "Join a private room by invite code")
    @PostMapping("/join/invite/{code}")
    public ResponseEntity<StudyRoomResponse> joinByInvite(
            @PathVariable String code,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                roomService.joinByInviteCode(code, currentUser));
    }

    // DELETE /api/rooms/{roomId}/leave
    @Operation(summary = "Leave a room (non-owners only)")
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User currentUser) {

        roomService.leaveRoom(roomId, currentUser);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/rooms/{roomId}
    @Operation(summary = "Delete a room (owner only)")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User currentUser) {

        roomService.deleteRoom(roomId, currentUser);
        return ResponseEntity.noContent().build();
    }

    // GET /api/rooms/{roomId}/members
    @Operation(summary = "Get all members of a room")
    @GetMapping("/{roomId}/members")
    public ResponseEntity<List<RoomMemberResponse>> getMembers(
            @PathVariable Long roomId) {

        return ResponseEntity.ok(roomService.getRoomMembers(roomId));
    }
}
