package com.sujan.studysync.controller;

import com.sujan.studysync.dto.request.ChangePasswordRequest;
import com.sujan.studysync.dto.request.UpdateProfileRequest;
import com.sujan.studysync.dto.response.ProfileResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Users", description = "Profile and settings management")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get my profile with full stats")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                userService.getMyProfile(currentUser));
    }

    @Operation(summary = "Update name, username, bio")
    @PatchMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                userService.updateProfile(request, currentUser));
    }

    @Operation(summary = "Upload avatar image")
    @PostMapping(
            value    = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProfileResponse> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                userService.uploadAvatar(file, currentUser));
    }

    @Operation(summary = "Change password (local accounts only)")
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {
        userService.changePassword(request, currentUser);
        return ResponseEntity.noContent().build();
    }
}
