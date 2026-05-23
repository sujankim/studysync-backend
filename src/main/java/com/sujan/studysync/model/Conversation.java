package com.sujan.studysync.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// A Conversation = one private chat thread between two users
// We always store the user with the SMALLER id as userOne
// This prevents creating two conversations between the same people
@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_one_id", "user_two_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;   // user with smaller id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;   // user with larger id

    // Last message text shown as preview in the conversation list
    // e.g. "Hey, want to study together?"
    @Column(length = 255)
    private String lastMessage;

    // When was the last message sent — used for sorting
    private LocalDateTime lastMessageAt;
}