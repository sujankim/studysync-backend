package com.sujan.studysync.service;

import com.sujan.studysync.dto.response.ResourceResponse;
import com.sujan.studysync.enums.ResourceType;
import com.sujan.studysync.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {

    // Upload a file (PDF, image, video, document)
    ResourceResponse uploadFile(
            Long roomId,
            MultipartFile file,
            String title,
            String description,
            User currentUser
    );

    // Add a link (no file upload)
    ResourceResponse addLink(
            Long roomId,
            String title,
            String description,
            String url,
            User currentUser
    );

    // Get all resources in a room
    List<ResourceResponse> getRoomResources(Long roomId);

    // Get resources filtered by type
    List<ResourceResponse> getRoomResourcesByType(
            Long roomId, ResourceType type);

    // Get resources uploaded by the current user in a room
    List<ResourceResponse> getMyUploads(Long roomId, User currentUser);

    // Delete a resource (uploader or room owner only)
    void deleteResource(Long resourceId, User currentUser);
}
