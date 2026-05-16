package com.sujan.studysync.repository;

import com.sujan.studysync.enums.ResourceType;
import com.sujan.studysync.model.Resource;
import com.sujan.studysync.model.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository
        extends JpaRepository<Resource, Long> {

    // All resources in a room, newest first
    List<Resource> findByRoomOrderByCreatedAtDesc(StudyRoom room);

    // Filter by type within a room
    // e.g. show only PDFs
    List<Resource> findByRoomAndTypeOrderByCreatedAtDesc(
            StudyRoom room, ResourceType type);

    // All resources uploaded by a specific user in a room
    // Used for "My Uploads" tab
    List<Resource> findByRoomAndUploadedByIdOrderByCreatedAtDesc(
            StudyRoom room, Long userId);
}
