package com.sujan.studysync.model;

import com.sujan.studysync.enums.RoomRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "room_members",
        uniqueConstraints = @UniqueConstraint(
                columnNames = { "room_id", "user_id" }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomRole role = RoomRole.MEMBER;

    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}