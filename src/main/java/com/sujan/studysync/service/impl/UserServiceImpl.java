package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.request.ChangePasswordRequest;
import com.sujan.studysync.dto.request.UpdateProfileRequest;
import com.sujan.studysync.dto.response.ProfileResponse;
import com.sujan.studysync.exception.InvalidCredentialsException;
import com.sujan.studysync.exception.UsernameAlreadyExistsException;
import com.sujan.studysync.model.User;
import com.sujan.studysync.model.UserStreak;
import com.sujan.studysync.repository.UserRepository;
import com.sujan.studysync.repository.UserStreakRepository;
import com.sujan.studysync.service.CloudinaryService;
import com.sujan.studysync.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository       userRepository;
    private final UserStreakRepository  streakRepository;
    private final CloudinaryService    cloudinaryService;
    private final PasswordEncoder      passwordEncoder;

    // ─── Get My Profile ───────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(User currentUser) {
        UserStreak streak = streakRepository
                .findByUser(currentUser)
                .orElseGet(() -> UserStreak.builder()
                        .user(currentUser).build());

        return toProfileResponse(currentUser, streak);
    }

    // ─── Update Profile ───────────────────────────────────────
    @Override
    @Transactional
    public ProfileResponse updateProfile(
            UpdateProfileRequest request,
            User currentUser) {

        // Update name if provided
        if (request.name() != null
                && !request.name().isBlank()) {
            currentUser.setName(request.name().trim());
        }

        // Update username if provided + not taken
        if (request.username() != null
                && !request.username().isBlank()) {
            String newUsername = request.username()
                    .toLowerCase().trim();

            if (!newUsername.equals(currentUser.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new UsernameAlreadyExistsException(
                            newUsername);
                }
                currentUser.setUsername(newUsername);
            }
        }

        // Update bio if provided (allow empty string to clear it)
        if (request.bio() != null) {
            currentUser.setBio(request.bio().trim());
        }

        User saved = userRepository.save(currentUser);

        UserStreak streak = streakRepository
                .findByUser(saved)
                .orElseGet(() -> UserStreak.builder()
                        .user(saved).build());

        return toProfileResponse(saved, streak);
    }

    // ─── Upload Avatar ────────────────────────────────────────
    @Override
    @Transactional
    public ProfileResponse uploadAvatar(
            MultipartFile file, User currentUser) {

        try {
            // Delete old Cloudinary avatar if exists
            // Extract public_id from existing URL
            if (currentUser.getAvatarUrl() != null
                    && currentUser.getAvatarUrl()
                    .contains("cloudinary.com")) {
                // We don't track avatarPublicId on user
                // so we just overwrite — old file stays but
                // it's just one small image, acceptable trade-off
            }

            Map<String, String> result = cloudinaryService.upload(
                    file,
                    "studysync/avatars"
            );

            currentUser.setAvatarUrl(result.get("url"));
            User saved = userRepository.save(currentUser);

            UserStreak streak = streakRepository
                    .findByUser(saved)
                    .orElseGet(() -> UserStreak.builder()
                            .user(saved).build());

            return toProfileResponse(saved, streak);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload avatar: " + e.getMessage());
        }
    }

    // ─── Change Password ──────────────────────────────────────
    @Override
    @Transactional
    public void changePassword(
            ChangePasswordRequest request,
            User currentUser) {

        // OAuth2 users have no password
        if (currentUser.getPassword() == null
                || currentUser.getPassword().isBlank()) {
            throw new RuntimeException(
                    "Cannot change password for social login accounts.");
        }

        // Verify current password
        if (!passwordEncoder.matches(
                request.currentPassword(),
                currentUser.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Set new password
        currentUser.setPassword(
                passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
    }

    // ─── Private ──────────────────────────────────────────────
    private ProfileResponse toProfileResponse(
            User user, UserStreak streak) {

        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getRole().name(),
                user.getPlan().name(),
                user.getProvider(),

                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getTotalDays(),
                streak.getTotalMinutes(),
                streak.getLastStudyDate(),
                streak.getRoomsJoined(),
                streak.getResourcesShared(),

                user.getCreatedAt()
        );
    }
}
