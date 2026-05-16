package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.response.ResourceResponse;
import com.sujan.studysync.enums.ResourceType;
import com.sujan.studysync.exception.ResourceNotFoundException;
import com.sujan.studysync.exception.RoomNotFoundException;
import com.sujan.studysync.exception.UnauthorizedException;
import com.sujan.studysync.mapper.ResourceMapper;
import com.sujan.studysync.model.Resource;
import com.sujan.studysync.model.StudyRoom;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.ResourceRepository;
import com.sujan.studysync.repository.RoomMemberRepository;
import com.sujan.studysync.repository.StudyRoomRepository;
import com.sujan.studysync.service.CloudinaryService;
import com.sujan.studysync.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final StudyRoomRepository roomRepository;
    private final RoomMemberRepository memberRepository;
    private final CloudinaryService cloudinaryService;
    private final ResourceMapper resourceMapper;

    // ─── Upload File ──────────────────────────────────────────
    @Override
    @Transactional
    public ResourceResponse uploadFile(
            Long roomId,
            MultipartFile file,
            String title,
            String description,
            User currentUser) {

        StudyRoom room = findRoomOrThrow(roomId);
        checkMembership(room, currentUser);

        try {
            // Determine resource type from MIME type
            // e.g. "application/pdf" → PDF
            //      "image/png"       → IMAGE
            //      "video/mp4"       → VIDEO
            String contentType  = file.getContentType();
            ResourceType type   = detectType(contentType);

            // Upload to Cloudinary
            // Files are organized in folders: studysync/rooms/{roomId}
            Map<String, String> uploadResult = cloudinaryService.upload(
                    file,
                    "studysync/rooms/" + roomId
            );

            // Save resource metadata to database
            Resource resource = Resource.builder()
                    .title(title != null && !title.isBlank()
                            ? title
                            : file.getOriginalFilename())  // fallback to filename
                    .description(description)
                    .type(type)
                    .url(uploadResult.get("url"))
                    .cloudinaryPublicId(uploadResult.get("public_id"))
                    .fileSize(file.getSize())
                    .originalFileName(file.getOriginalFilename())
                    .room(room)
                    .uploadedBy(currentUser)
                    .build();

            return resourceMapper.toResponse(
                    resourceRepository.save(resource));

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload file: " + e.getMessage(), e);
        }
    }

    // ─── Add Link ─────────────────────────────────────────────
    @Override
    @Transactional
    public ResourceResponse addLink(
            Long roomId,
            String title,
            String description,
            String url,
            User currentUser) {

        StudyRoom room = findRoomOrThrow(roomId);
        checkMembership(room, currentUser);

        Resource resource = Resource.builder()
                .title(title)
                .description(description)
                .type(ResourceType.LINK)
                .url(url)
                // No cloudinaryPublicId — it's just a URL
                .room(room)
                .uploadedBy(currentUser)
                .build();

        return resourceMapper.toResponse(
                resourceRepository.save(resource));
    }

    // ─── Get All Resources ────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getRoomResources(Long roomId) {
        StudyRoom room = findRoomOrThrow(roomId);
        return resourceRepository
                .findByRoomOrderByCreatedAtDesc(room)
                .stream()
                .map(resourceMapper::toResponse)
                .toList();
    }

    // ─── Filter by Type ───────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getRoomResourcesByType(
            Long roomId, ResourceType type) {
        StudyRoom room = findRoomOrThrow(roomId);
        return resourceRepository
                .findByRoomAndTypeOrderByCreatedAtDesc(room, type)
                .stream()
                .map(resourceMapper::toResponse)
                .toList();
    }

    // ─── My Uploads ───────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getMyUploads(
            Long roomId, User currentUser) {
        StudyRoom room = findRoomOrThrow(roomId);
        return resourceRepository
                .findByRoomAndUploadedByIdOrderByCreatedAtDesc(
                        room, currentUser.getId())
                .stream()
                .map(resourceMapper::toResponse)
                .toList();
    }

    // ─── Delete Resource ──────────────────────────────────────
    @Override
    @Transactional
    public void deleteResource(Long resourceId, User currentUser) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(resourceId));

        // Only the uploader or room owner can delete
        boolean isUploader = resource.getUploadedBy()
                .getId().equals(currentUser.getId());

        boolean isOwner = memberRepository
                .findByRoomAndUser(resource.getRoom(), currentUser)
                .map(m -> m.getRole().name().equals("OWNER"))
                .orElse(false);

        if (!isUploader && !isOwner) {
            throw new UnauthorizedException(
                    "You can only delete your own resources.");
        }

        // Delete from Cloudinary if it was a file upload
        if (resource.getCloudinaryPublicId() != null) {
            String cloudinaryType = cloudinaryService
                    .getCloudinaryResourceType(
                            detectMimeFromType(resource.getType()));
            cloudinaryService.delete(
                    resource.getCloudinaryPublicId(), cloudinaryType);
        }

        resourceRepository.delete(resource);
    }

    // ─── Private Helpers ──────────────────────────────────────

    private StudyRoom findRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    private void checkMembership(StudyRoom room, User user) {
        if (!memberRepository.existsByRoomAndUser(room, user)) {
            throw new UnauthorizedException(
                    "Only room members can manage resources.");
        }
    }

    // Detect ResourceType from MIME type string
    // contentType = "application/pdf", "image/png", "video/mp4" etc.
    private ResourceType detectType(String contentType) {
        if (contentType == null) return ResourceType.DOCUMENT;

        if (contentType.equals("application/pdf"))
            return ResourceType.PDF;

        if (contentType.startsWith("image/"))
            return ResourceType.IMAGE;

        if (contentType.startsWith("video/"))
            return ResourceType.VIDEO;

        // Word, Excel, PowerPoint, etc.
        return ResourceType.DOCUMENT;
    }

    // Reverse lookup: ResourceType → MIME prefix
    // Used to tell Cloudinary which resource_type to use when deleting
    private String detectMimeFromType(ResourceType type) {
        return switch (type) {
            case IMAGE    -> "image/png";
            case VIDEO    -> "video/mp4";
            default       -> "application/pdf"; // "raw" in Cloudinary
        };
    }
}
