package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.response.MessageResponse;
import com.sujan.studysync.exception.NotMemberException;
import com.sujan.studysync.exception.RoomNotFoundException;
import com.sujan.studysync.mapper.MessageMapper;
import com.sujan.studysync.model.Message;
import com.sujan.studysync.model.StudyRoom;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.MessageRepository;
import com.sujan.studysync.repository.RoomMemberRepository;
import com.sujan.studysync.repository.StudyRoomRepository;
import com.sujan.studysync.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final StudyRoomRepository roomRepository;
    private final RoomMemberRepository memberRepository;
    private final MessageMapper messageMapper;

    // ─── Save a new message ───────────────────────────────────
    @Override
    @Transactional
    public MessageResponse saveMessage(
            Long roomId, String content, User sender) {

        // 1. Find the room — throw 404 if not found
        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        // 2. Only room members can send messages
        if (!memberRepository.existsByRoomAndUser(room, sender)) {
            throw new NotMemberException();
        }

        // 3. Build and save the message
        Message message = Message.builder()
                .content(content.trim())   // trim whitespace
                .room(room)
                .sender(sender)
                .build();

        Message saved = messageRepository.save(message);

        // 4. Convert to response DTO using mapper
        return messageMapper.toResponse(saved);
    }

    // ─── Get chat history ─────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getChatHistory(
            Long roomId, Pageable pageable) {

        StudyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        // findByRoomOrderByCreatedAtDesc = newest first
        // Then we reverse them in the frontend so oldest shows at top
        return messageRepository
                .findByRoomOrderByCreatedAtDesc(room, pageable)
                .map(messageMapper::toResponse);
    }
}
