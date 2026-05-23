package com.sujan.studysync.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "direct_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectMessage extends BaseEntity {

    // Which conversation this message belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // Who sent this message
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Has the recipient read this message?
    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;
}