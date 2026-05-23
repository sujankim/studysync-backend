package com.sujan.studysync.repository;

import com.sujan.studysync.model.Conversation;
import com.sujan.studysync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    // All conversations for a user, newest message first
    // Uses JOIN FETCH to avoid LazyInitializationException
    @Query("""
        SELECT c FROM Conversation c
        JOIN FETCH c.userOne
        JOIN FETCH c.userTwo
        WHERE c.userOne = :user OR c.userTwo = :user
        ORDER BY
            c.lastMessageAt DESC NULLS LAST,
            c.createdAt DESC
        """)
    List<Conversation> findByUser(@Param("user") User user);

    // Check if a conversation already exists between two users
    @Query("""
        SELECT c FROM Conversation c
        WHERE (c.userOne = :u1 AND c.userTwo = :u2)
           OR (c.userOne = :u2 AND c.userTwo = :u1)
        """)
    Optional<Conversation> findBetween(
            @Param("u1") User u1,
            @Param("u2") User u2);

    // Same but with user loading to avoid lazy issues
    @Query("""
        SELECT c FROM Conversation c
        JOIN FETCH c.userOne
        JOIN FETCH c.userTwo
        WHERE c.id = :id
        """)
    Optional<Conversation> findByIdWithUsers(
            @Param("id") Long id);
}