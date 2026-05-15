package com.sujan.studysync.repository;

import com.sujan.studysync.model.Message;
import com.sujan.studysync.model.StudyRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Get chat history for a room, newest first (paginated)
    // "Desc" = newest first so we load recent messages at the bottom
    Page<Message> findByRoomOrderByCreatedAtDesc(
            StudyRoom room, Pageable pageable);
}
