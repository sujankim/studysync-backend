package com.sujan.studysync.model;

import com.sujan.studysync.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;

// A Resource is any file or link shared inside a study room
// Could be a PDF, image, video, link, or document
@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {

    // The display name shown in the UI e.g. "Java Cheat Sheet"
    @Column(nullable = false, length = 255)
    private String title;

    // Optional — "This PDF covers Java generics"
    @Column(columnDefinition = "TEXT")
    private String description;

    // What kind of resource: PDF, IMAGE, VIDEO, LINK, DOCUMENT
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    // Where the resource lives
    // For files: Cloudinary URL (e.g. https://res.cloudinary.com/...)
    // For links: the URL the user pasted
    @Column(nullable = false, length = 1000)
    private String url;

    // Cloudinary's public ID — we need this to DELETE the file later
    // null for LINK type (no Cloudinary file)
    @Column(length = 500)
    private String cloudinaryPublicId;

    // File size in bytes — null for links
    private Long fileSize;

    // Original filename e.g. "java_notes.pdf"
    @Column(length = 255)
    private String originalFileName;

    // Which room this resource belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    // Who uploaded this resource
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
}
