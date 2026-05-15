package com.sujan.studysync.service;

import com.sujan.studysync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Tracks which users are currently connected (online)
// Updates the isOnline field on the User entity
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final UserRepository userRepository;

    // Called when a WebSocket connection opens
    @Transactional
    public void markOnline(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsOnline(true);
            userRepository.save(user);
        });
    }

    // Called when a WebSocket connection closes
    @Transactional
    public void markOffline(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsOnline(false);
            userRepository.save(user);
        });
    }
}

