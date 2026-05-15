package com.sujan.studysync.event;

import com.sujan.studysync.dto.response.OnlineStatusResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

// Spring fires events when WebSocket clients connect/disconnect
// We listen to those events to update online status
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    // SimpMessagingTemplate = the tool to SEND messages from server → clients

    // ─── User connected ───────────────────────────────────────
    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        User user = extractUser(principal);
        if (user == null) return;

        presenceService.markOnline(user.getId());
        log.info("WebSocket connected: {}", user.getUsername());
    }

    // ─── User disconnected ────────────────────────────────────
    // When a user closes the browser tab / loses internet
    // Spring automatically fires this event
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        User user = extractUser(principal);
        if (user == null) return;

        presenceService.markOffline(user.getId());
        log.info("WebSocket disconnected: {}", user.getUsername());

        // Broadcast to all rooms that this user went offline
        // We use /topic/presence as a general presence channel
        OnlineStatusResponse offlineStatus = new OnlineStatusResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getAvatarUrl(),
                false   // isOnline = false
        );

        messagingTemplate.convertAndSend(
                "/topic/presence/" + user.getId(),
                offlineStatus
        );
    }

    // ─── Helper ───────────────────────────────────────────────
    private User extractUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Object p = auth.getPrincipal();
            if (p instanceof User user) return user;
        }
        return null;
    }
}

