package com.sujan.studysync.service;

import com.sujan.studysync.dto.response.MessageResponse;
import com.sujan.studysync.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    // Save message to DB and return the response DTO
    MessageResponse saveMessage(Long roomId, String content, User sender);

    // Get paginated chat history for a room
    Page<MessageResponse> getChatHistory(Long roomId, Pageable pageable);
}