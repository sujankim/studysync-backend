package com.sujan.studysync.repository;

import com.sujan.studysync.model.Notification;
import com.sujan.studysync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // All notifications for a user — newest first
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Count unread — used for the sidebar badge
    long countByUserAndIsReadFalse(User user);

    // Find by id + user — used for delete security check
    Optional<Notification> findByIdAndUser(Long id, User user);

    // Mark ALL unread as read in one SQL UPDATE
    // Much faster than loading each one and setting isRead=true
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true
        WHERE n.user = :user
        AND n.isRead = false
        """)
    void markAllRead(@Param("user") User user);
}