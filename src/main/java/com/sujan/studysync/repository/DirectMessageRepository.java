package com.sujan.studysync.repository;

import com.sujan.studysync.model.Conversation;
import com.sujan.studysync.model.DirectMessage;
import com.sujan.studysync.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectMessageRepository
        extends JpaRepository<DirectMessage, Long> {

    // Message history for a conversation — newest first
    // Frontend reverses the list to show oldest at top
    Page<DirectMessage> findByConversationOrderByCreatedAtDesc(
            Conversation conversation,
            Pageable pageable);

    // Mark all messages in a conversation as read
    // Only marks messages where sender != reader
    // (you don't need to "read" your own messages)
    @Modifying
    @Query("""
        UPDATE DirectMessage dm
        SET dm.isRead = true
        WHERE dm.conversation = :conversation
        AND dm.sender != :reader
        AND dm.isRead = false
        """)
    void markReadInConversation(
            @Param("conversation") Conversation conversation,
            @Param("reader") User reader);

    // Total unread DMs across ALL conversations for a user
    // Used for the sidebar badge number
    @Query("""
        SELECT COUNT(dm)
        FROM DirectMessage dm
        JOIN dm.conversation c
        WHERE (c.userOne = :user OR c.userTwo = :user)
        AND dm.sender != :user
        AND dm.isRead = false
        """)
    long countUnread(@Param("user") User user);

    // Unread count in ONE specific conversation for a user
    @Query("""
        SELECT COUNT(dm)
        FROM DirectMessage dm
        WHERE dm.conversation = :conv
        AND dm.sender != :reader
        AND dm.isRead = false
        """)
    long countUnreadInConversation(
            @Param("conv") Conversation conv,
            @Param("reader") User reader);
}