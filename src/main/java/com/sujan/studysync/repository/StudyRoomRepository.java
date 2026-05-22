package com.sujan.studysync.repository;

import com.sujan.studysync.model.StudyRoom;
import com.sujan.studysync.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    // Browse all public rooms newest first
    Page<StudyRoom> findByIsPrivateFalseOrderByCreatedAtDesc(Pageable pageable);

    // Filter by topic
    Page<StudyRoom> findByIsPrivateFalseAndTopicIgnoreCaseOrderByCreatedAtDesc(
            String topic, Pageable pageable);

    // Search by name / description / topic
    @Query("""
            SELECT r FROM StudyRoom r
            WHERE r.isPrivate = false
            AND (
                LOWER(r.name)        LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(r.description) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(r.topic)       LIKE LOWER(CONCAT('%', :q, '%'))
            )
            ORDER BY r.createdAt DESC
            """)
    Page<StudyRoom> searchPublic(@Param("q") String query, Pageable pageable);

    // All rooms a user has joined (any role)
    @Query("""
            SELECT r FROM StudyRoom r
            JOIN r.members m
            WHERE m.user = :user
            ORDER BY m.joinedAt DESC
            """)
    List<StudyRoom> findByMember(@Param("user") User user);

    boolean existsBySlug(String slug);

    Optional<StudyRoom> findBySlug(String slug);

    Optional<StudyRoom> findByInviteCode(String inviteCode);

    // Get IDs of all rooms the current user has joined
    // One query → a Set of Long IDs → fast O(1) lookup
    @Query("""
    SELECT m.room.id FROM RoomMember m
    WHERE m.user = :user
    """)
    Set<Long> findRoomIdsByMember(@Param("user") User user);
}
