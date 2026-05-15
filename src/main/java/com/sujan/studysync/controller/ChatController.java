package com.sujan.studysync.controller;

import com.sujan.studysync.dto.payload.ChatMessagePayload;
import com.sujan.studysync.dto.payload.TypingPayload;
import com.sujan.studysync.dto.response.MessageResponse;
import com.sujan.studysync.dto.response.TypingResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// This controller handles BOTH:
// 1. @MessageMapping → WebSocket messages (real-time)
// 2. @GetMapping     → REST HTTP (chat history)

@Tag(name = "Chat",
        description = "Real-time messaging and chat history")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    // SimpMessagingTemplate = tool to broadcast to /topic/...

    // ─── WebSocket: Send Message ──────────────────────────────
    // Angular sends to: /app/room/{roomId}/send
    // We save it and broadcast to: /topic/room/{roomId}
    @MessageMapping("/room/{roomId}/send")
    public void sendMessage(
            @DestinationVariable Long roomId,
            // @Payload = the JSON body of the WebSocket message
            @Payload ChatMessagePayload payload,
            // @AuthenticationPrincipal works in WebSocket too
            // because our WebSocketAuthInterceptor set the user
            @AuthenticationPrincipal User currentUser) {

        // 1. Save message to database
        MessageResponse saved = messageService.saveMessage(
                roomId, payload.content(), currentUser);

        // 2. Broadcast to ALL subscribers of this room's topic
        // Every user in the room receives this instantly
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                saved
        );
    }

    // ─── WebSocket: Typing Indicator ──────────────────────────
    // Angular sends to: /app/room/{roomId}/typing
    // We broadcast to: /topic/room/{roomId}/typing
    // Other users see "Sujan is typing..."
    @MessageMapping("/room/{roomId}/typing")
    public void handleTyping(
            @DestinationVariable Long roomId,
            @Payload TypingPayload payload,
            @AuthenticationPrincipal User currentUser) {

        TypingResponse response = new TypingResponse(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getName(),
                payload.isTyping()
        );

        // Broadcast typing status to all room members
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/typing",
                response
        );
    }

    // ─── REST: Chat History ───────────────────────────────────
    // HTTP GET — load previous messages when first opening a room
    // WebSocket only handles NEW messages going forward
    @Operation(
            summary = "Get chat history for a room",
            description = "Returns paginated messages. Page 0 = most recent."
    )
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatHistory(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User currentUser) {

        // Load newest 50 messages (page 0 = most recent)
        return ResponseEntity.ok(
                messageService.getChatHistory(
                        roomId,
                        PageRequest.of(page, size)
                )
        );
    }
}