package com.sujan.studysync.service;

import com.sujan.studysync.enums.Plan;
import com.sujan.studysync.enums.UserRole;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.UserRepository;
import com.sujan.studysync.security.oauth.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService {

    private final UserRepository userRepository;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    @Transactional
    public User processOAuthUser(OAuth2UserInfo userInfo) {

        String email = userInfo.getEmail().toLowerCase();

        User existingUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (existingUser != null) {

            // Link provider if different
            if (existingUser.getProvider() == null ||
                    !existingUser.getProvider().equals(userInfo.getProvider())) {

                existingUser.setProvider(userInfo.getProvider());
                existingUser.setProviderId(userInfo.getProviderId());
            }

            // Update profile picture
            if (userInfo.getImageUrl() != null) {
                existingUser.setAvatarUrl(userInfo.getImageUrl());
            }

            // Update name if missing
            if (existingUser.getName() == null ||
                    existingUser.getName().isBlank()) {

                existingUser.setName(userInfo.getName());
            }

            return userRepository.save(existingUser);
        }

        User user = User.builder()
                .email(email)
                .name(userInfo.getName())
                .username(generateUniqueUsername(email))
                .avatarUrl(userInfo.getImageUrl())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .role(UserRole.ROLE_USER)
                .plan(Plan.FREE)
                .build();

        return userRepository.save(user);
    }

    private String generateUniqueUsername(String email) {

        String baseUsername = email
                .split("@")[0]
                .replaceAll("[^a-zA-Z0-9_]", "_")
                .toLowerCase();

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        return username;
    }
}