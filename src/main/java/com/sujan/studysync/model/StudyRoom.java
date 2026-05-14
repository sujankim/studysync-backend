package com.sujan.studysync.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyRoom extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    // URL-friendly name e.g. "java-champions-a3f9k2"
    @Column(unique = true, nullable = false, length = 150)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String topic;

    // comma-separated: "java,spring,backend"
    @Column(length = 500)
    private String tags;

    @Column(length = 500)
    private String coverImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPrivate = false;

    // generated only for private rooms
    @Column(length = 20)
    private String inviteCode;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxMembers = 50;

    // the creator
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // all members (including owner)
    @OneToMany(
            mappedBy = "room",
            cascade  = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<RoomMember> members = new ArrayList<>();

    // ─── Helper ──────────────────────────────────────────────
    public int getMemberCount() {
        return members.size();
    }
}
