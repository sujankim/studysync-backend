package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.response.NotificationResponse;
import com.sujan.studysync.exception.UnauthorizedException;
import com.sujan.studysync.model.Notification;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.NotificationRepository;
import com.sujan.studysync.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;

    // SimpMessagingTemplate = tool to push messages to WebSocket clients
    // We use this to send real-time notifications to Angular
    private final SimpMessagingTemplate  messagingTemplate;

    // ─── Get all notifications ────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(
            User currentUser) {

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Get unread count ─────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(User currentUser) {
        return notificationRepository
                .countByUserAndIsReadFalse(currentUser);
    }

    // ─── Mark all read ────────────────────────────────────────
    @Override
    @Transactional
    public void markAllRead(User currentUser) {
        notificationRepository.markAllRead(currentUser);
    }

    // ─── Delete one notification ──────────────────────────────
    @Override
    @Transactional
    public void deleteNotification(Long id, User currentUser) {

        Notification notification = notificationRepository
                .findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new UnauthorizedException(
                        "Notification not found or access denied."));

        notificationRepository.delete(notification);
    }

    // ─── Create + push via WebSocket ──────────────────────────
    // Called internally by other services
    // e.g. when someone joins a room → notify the room owner
    @Override
    @Transactional
    public void createNotification(
            User   recipient,
            String type,
            String title,
            String message,
            Long   referenceId,
            String actionUrl) {

        // 1. Save to database so user sees it after refresh
        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .referenceId(referenceId)
                .actionUrl(actionUrl)
                .build();

        Notification saved =
                notificationRepository.save(notification);

        // 2. Push to user's browser in REAL TIME via WebSocket
        // convertAndSendToUser = sends to ONE specific user only
        // Angular subscribes to /user/queue/notifications
        // Spring uses recipient email as the user identifier
        NotificationResponse response = toResponse(saved);

        try {
            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(),    // ← which user
                    "/queue/notifications", // ← their private channel
                    response
            );
            log.info("Notification pushed to: {}",
                    recipient.getEmail());
        } catch (Exception e) {
            // Don't crash if WebSocket push fails
            // The notification is already saved in DB
            log.warn("Failed to push notification to {}: {}",
                    recipient.getEmail(), e.getMessage());
        }
    }

    // ─── Helper ───────────────────────────────────────────────
    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getIsRead(),
                n.getReferenceId(),
                n.getActionUrl(),
                n.getCreatedAt()
        );
    }
}