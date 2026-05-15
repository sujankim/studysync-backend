package com.sujan.studysync.model;

import jakarta.persistence.*;
import lombok.*;

// Stores every chat message sent in a room
// Each message belongs to one room and one sender
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    // The TEXT content of the message
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Which room this message belongs to
    // LAZY = don't load the full room object unless we need it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    // Who sent this message
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Has this message been edited after sending?
    @Builder.Default
    private Boolean isEdited = false;
}

