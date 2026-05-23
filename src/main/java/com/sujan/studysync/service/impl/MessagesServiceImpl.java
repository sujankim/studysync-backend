package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.response.ConversationResponse;
import com.sujan.studysync.dto.response.DirectMessageResponse;
import com.sujan.studysync.exception.UnauthorizedException;
import com.sujan.studysync.exception.UserNotFoundException;
import com.sujan.studysync.mapper.UserMapper;
import com.sujan.studysync.model.Conversation;
import com.sujan.studysync.model.DirectMessage;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.ConversationRepository;
import com.sujan.studysync.repository.DirectMessageRepository;
import com.sujan.studysync.repository.UserRepository;
import com.sujan.studysync.service.MessagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessagesServiceImpl implements MessagesService {

    private final ConversationRepository  conversationRepository;
    private final DirectMessageRepository dmRepository;
    private final UserRepository          userRepository;
    private final UserMapper              userMapper;

    // ─── Get Conversations ────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(
            User currentUser) {

        return conversationRepository
                .findByUser(currentUser)
                .stream()
                .map(c -> toConversationResponse(c, currentUser))
                .toList();
    }

    // ─── Get One Conversation ─────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(
            Long conversationId, User currentUser) {

        Conversation conv = findAndCheckAccess(
                conversationId, currentUser);
        return toConversationResponse(conv, currentUser);
    }

    // ─── Get or Create ────────────────────────────────────────
    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(
            Long otherUserId, User currentUser) {

        // Cannot DM yourself
        if (currentUser.getId().equals(otherUserId)) {
            throw new UnauthorizedException(
                    "You cannot start a conversation with yourself.");
        }

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found: " + otherUserId));

        // Return existing conversation if it exists
        return conversationRepository
                .findBetween(currentUser, otherUser)
                .map(c -> toConversationResponse(c, currentUser))
                .orElseGet(() -> {
                    // Create new conversation
                    // Always store smaller id as userOne
                    // This guarantees uniqueness via the DB constraint
                    User userOne = currentUser.getId()
                            < otherUser.getId()
                            ? currentUser : otherUser;
                    User userTwo = userOne.getId()
                            .equals(currentUser.getId())
                            ? otherUser : currentUser;

                    Conversation saved =
                            conversationRepository.save(
                                    Conversation.builder()
                                            .userOne(userOne)
                                            .userTwo(userTwo)
                                            .build());

                    return toConversationResponse(
                            saved, currentUser);
                });
    }

    // ─── Get Messages ─────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<DirectMessageResponse> getMessages(
            Long conversationId,
            Pageable pageable,
            User currentUser) {

        Conversation conv = findAndCheckAccess(
                conversationId, currentUser);

        return dmRepository
                .findByConversationOrderByCreatedAtDesc(
                        conv, pageable)
                .map(this::toDmResponse);
    }

    // ─── Send Message ─────────────────────────────────────────
    @Override
    @Transactional
    public DirectMessageResponse sendMessage(
            Long conversationId,
            String content,
            User sender) {

        Conversation conv = findAndCheckAccess(
                conversationId, sender);

        // Save message
        DirectMessage dm = DirectMessage.builder()
                .conversation(conv)
                .sender(sender)
                .content(content.trim())
                .isRead(false)
                .build();

        DirectMessage saved = dmRepository.save(dm);

        // Update conversation preview
        String preview = content.length() > 60
                ? content.substring(0, 60) + "…"
                : content;
        conv.setLastMessage(preview);
        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conv);

        return toDmResponse(saved);
    }

    // ─── Mark Read ────────────────────────────────────────────
    @Override
    @Transactional
    public void markRead(Long conversationId, User currentUser) {
        Conversation conv = findAndCheckAccess(
                conversationId, currentUser);
        dmRepository.markReadInConversation(conv, currentUser);
    }

    // ─── Unread Count ─────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(User currentUser) {
        return dmRepository.countUnread(currentUser);
    }

    // ─── Private Helpers ──────────────────────────────────────

    // Find conversation + verify the user is a participant
    private Conversation findAndCheckAccess(
            Long convId, User currentUser) {

        Conversation conv = conversationRepository
                .findByIdWithUsers(convId)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Conversation not found."));

        boolean isParticipant =
                conv.getUserOne().getId()
                        .equals(currentUser.getId())
                        || conv.getUserTwo().getId()
                        .equals(currentUser.getId());

        if (!isParticipant) {
            throw new UnauthorizedException(
                    "You are not a participant in this conversation.");
        }

        return conv;
    }

    private ConversationResponse toConversationResponse(
            Conversation conv, User currentUser) {

        // The "other" user is whoever is NOT the current user
        User other = conv.getUserOne().getId()
                .equals(currentUser.getId())
                ? conv.getUserTwo()
                : conv.getUserOne();

        // Count unread messages in this specific conversation
        long unread = dmRepository.countUnreadInConversation(
                conv, currentUser);

        return new ConversationResponse(
                conv.getId(),
                userMapper.toResponse(other),
                conv.getLastMessage(),
                conv.getLastMessageAt(),
                unread,
                conv.getCreatedAt()
        );
    }

    private DirectMessageResponse toDmResponse(
            DirectMessage dm) {

        return new DirectMessageResponse(
                dm.getId(),
                dm.getConversation().getId(),
                userMapper.toResponse(dm.getSender()),
                dm.getContent(),
                dm.getIsRead(),
                dm.getCreatedAt()
        );
    }
}