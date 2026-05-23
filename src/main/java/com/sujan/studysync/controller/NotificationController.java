package com.sujan.studysync.controller;

import com.sujan.studysync.dto.response.NotificationResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Notifications",
        description = "User notification management")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications
    @Operation(summary = "Get all notifications for current user")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                notificationService.getNotifications(currentUser));
    }

    // GET /api/notifications/unread-count
    @Operation(summary = "Get unread notification count")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                Map.of("count",
                        notificationService
                                .getUnreadCount(currentUser)));
    }

    // PATCH /api/notifications/read-all
    @Operation(summary = "Mark all notifications as read")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal User currentUser) {

        notificationService.markAllRead(currentUser);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/notifications/{id}
    @Operation(summary = "Delete a notification")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        notificationService.deleteNotification(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}