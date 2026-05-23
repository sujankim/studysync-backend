package com.sujan.studysync.service;

import com.sujan.studysync.dto.response.ConversationResponse;
import com.sujan.studysync.dto.response.DirectMessageResponse;
import com.sujan.studysync.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessagesService {

    // Get all conversations for the current user
    List<ConversationResponse> getConversations(User currentUser);

    // Get a single conversation by id
    ConversationResponse getConversationById(
            Long conversationId, User currentUser);

    // Get or create a conversation with another user
    ConversationResponse getOrCreateConversation(
            Long otherUserId, User currentUser);

    // Get paginated message history
    Page<DirectMessageResponse> getMessages(
            Long conversationId,
            Pageable pageable,
            User currentUser);

    // Send a DM — called from WebSocket controller
    DirectMessageResponse sendMessage(
            Long conversationId,
            String content,
            User sender);

    // Mark all messages in a conversation as read
    void markRead(Long conversationId, User currentUser);

    // Total unread DM count for sidebar badge
    long getUnreadCount(User currentUser);
}