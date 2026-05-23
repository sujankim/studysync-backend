package com.sujan.studysync.controller;

import com.sujan.studysync.dto.payload.DirectMessagePayload;
import com.sujan.studysync.dto.response.ConversationResponse;
import com.sujan.studysync.dto.response.DirectMessageResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.MessagesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Messages",
        description = "Direct messaging between users")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagesController {

    private final MessagesService       messagesService;
    private final SimpMessagingTemplate messagingTemplate;

    // ─── GET /api/messages/conversations ──────────────────────
    @Operation(summary = "Get all conversations for current user")
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>>
    getConversations(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                messagesService.getConversations(currentUser));
    }

    // ─── GET /api/messages/conversations/{id} ─────────────────
    @Operation(summary = "Get a single conversation by id")
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse>
    getConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                messagesService.getConversationById(
                        conversationId, currentUser));
    }

    // ─── POST /api/messages/conversations/{userId} ────────────
    // Get existing OR create new conversation with a user
    @Operation(
            summary = "Get or create a conversation with a user",
            description = "If conversation already exists, returns it. " +
                    "Otherwise creates and returns a new one."
    )
    @PostMapping("/conversations/{userId}")
    public ResponseEntity<ConversationResponse>
    getOrCreate(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                messagesService.getOrCreateConversation(
                        userId, currentUser));
    }

    // ─── GET /api/messages/conversations/{id}/messages ────────
    @Operation(
            summary = "Get message history in a conversation",
            description = "Returns messages newest-first (page 0 = " +
                    "most recent). Frontend reverses for display."
    )
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Page<DirectMessageResponse>>
    getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                messagesService.getMessages(
                        conversationId,
                        PageRequest.of(page, size),
                        currentUser));
    }

    // ─── PATCH /api/messages/conversations/{id}/read ──────────
    @Operation(summary = "Mark all messages in conversation as read")
    @PatchMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User currentUser) {

        messagesService.markRead(conversationId, currentUser);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /api/messages/unread-count ───────────────────────
    @Operation(summary = "Get total unread DM count")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                Map.of("count",
                        messagesService.getUnreadCount(
                                currentUser)));
    }

    // ─── WebSocket: Send DM ───────────────────────────────────
    // Angular sends to: /app/dm/send
    // Spring saves it and pushes to BOTH users' private channels
    @MessageMapping("/dm/send")
    public void sendDm(
            @Payload DirectMessagePayload payload,
            @AuthenticationPrincipal User sender) {

        log.debug("DM from {} to conv {}",
                sender.getEmail(), payload.conversationId());

        // Save message to DB
        DirectMessageResponse message = messagesService.sendMessage(
                payload.conversationId(),
                payload.content(),
                sender);

        // Get the conversation to find the recipient
        ConversationResponse conv = messagesService
                .getConversationById(
                        payload.conversationId(), sender);

        String senderEmail    = sender.getEmail();
        String recipientEmail = conv.otherUser().email();

        // Push to sender (so their UI updates immediately)
        messagingTemplate.convertAndSendToUser(
                senderEmail,
                "/queue/dm",
                message
        );

        // Push to recipient (real-time delivery)
        messagingTemplate.convertAndSendToUser(
                recipientEmail,
                "/queue/dm",
                message
        );
    }
}