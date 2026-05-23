package com.sujan.studysync.service;

import com.sujan.studysync.dto.request.ChangePasswordRequest;
import com.sujan.studysync.dto.request.UpdateProfileRequest;
import com.sujan.studysync.dto.response.ProfileResponse;
import com.sujan.studysync.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    ProfileResponse getMyProfile(User currentUser);

    ProfileResponse updateProfile(
            UpdateProfileRequest request,
            User currentUser);

    ProfileResponse uploadAvatar(
            MultipartFile file,
            User currentUser);

    void changePassword(
            ChangePasswordRequest request,
            User currentUser);
}
