package com.sujan.studysync.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    // Who receives this notification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Type constants:
    // ROOM_JOINED     → someone joined your room
    // ROOM_MESSAGE    → new message in your room
    // RESOURCE_SHARED → someone shared a resource
    // STREAK_REMINDER → you haven't studied today
    // SYSTEM          → general announcements
    @Column(nullable = false, length = 50)
    private String type;

    // Short title shown in UI e.g. "Sujan joined your room"
    @Column(nullable = false, length = 255)
    private String title;

    // Longer description e.g. "Sujan joined Java Champions"
    @Column(columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    // Which room/resource this notification relates to
    // e.g. roomId = 5 for a ROOM_JOINED notification
    private Long referenceId;

    // Where to navigate when user clicks
    // e.g. "/rooms/5"
    @Column(length = 255)
    private String actionUrl;
}