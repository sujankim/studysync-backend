package com.sujan.studysync.service;

import com.sujan.studysync.dto.response.NotificationResponse;
import com.sujan.studysync.model.User;

import java.util.List;

public interface NotificationService {

    // ─── Read operations ──────────────────────────────────────
    List<NotificationResponse> getNotifications(User currentUser);

    long getUnreadCount(User currentUser);

    // ─── Write operations ─────────────────────────────────────
    void markAllRead(User currentUser);

    void deleteNotification(Long id, User currentUser);

    // ─── Internal — called by other services ──────────────────
    // e.g. RoomServiceImpl calls this when someone joins a room
    void createNotification(
            User   recipient,
            String type,
            String title,
            String message,
            Long   referenceId,
            String actionUrl
    );
}