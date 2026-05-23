package com.sujan.studysync.controller;

import com.sujan.studysync.dto.response.ResourceResponse;
import com.sujan.studysync.enums.ResourceType;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Resources",
        description = "Upload and manage study room resources")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    // ═══════════════════════════════════════════════════════════
    // GLOBAL ENDPOINTS — /api/resources/...
    // ═══════════════════════════════════════════════════════════

    // ─── GET /api/resources/my ────────────────────────────────
    // Returns ALL resources from ALL rooms the user has joined
    // Used by the global /resources page in Angular
    @Operation(
            summary     = "Get all resources across all joined rooms",
            description = "Returns every resource from every room " +
                    "the current user is a member of, sorted newest first."
    )
    @GetMapping("/api/resources/my")
    public ResponseEntity<List<ResourceResponse>> getAllMyResources(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                resourceService.getAllMyResources(currentUser));
    }

    // ═══════════════════════════════════════════════════════════
    // ROOM-SCOPED ENDPOINTS — /api/rooms/{roomId}/resources/...
    // ═══════════════════════════════════════════════════════════

    // ─── POST /api/rooms/{roomId}/resources/upload ────────────
    @Operation(
            summary     = "Upload a file to a room",
            description = "Supports PDF, images, videos, documents. Max 50MB."
    )
    @PostMapping(
            value    = "/api/rooms/{roomId}/resources/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResourceResponse> uploadFile(
            @PathVariable Long roomId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "title",       required = false)
            String title,
            @RequestPart(value = "description", required = false)
            String description,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceService.uploadFile(
                        roomId, file, title, description, currentUser));
    }

    // ─── POST /api/rooms/{roomId}/resources/link ──────────────
    @Operation(summary = "Add a link resource to a room")
    @PostMapping("/api/rooms/{roomId}/resources/link")
    public ResponseEntity<ResourceResponse> addLink(
            @PathVariable Long roomId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String url,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceService.addLink(
                        roomId, title, description, url, currentUser));
    }

    // ─── GET /api/rooms/{roomId}/resources ────────────────────
    @Operation(summary = "Get all resources in a room")
    @GetMapping("/api/rooms/{roomId}/resources")
    public ResponseEntity<List<ResourceResponse>> getResources(
            @PathVariable Long roomId,
            @RequestParam(required = false) ResourceType type,
            @AuthenticationPrincipal User currentUser) {

        if (type != null) {
            return ResponseEntity.ok(
                    resourceService.getRoomResourcesByType(
                            roomId, type));
        }

        return ResponseEntity.ok(
                resourceService.getRoomResources(roomId));
    }

    // ─── GET /api/rooms/{roomId}/resources/my ─────────────────
    @Operation(summary = "Get resources uploaded by the current user in a room")
    @GetMapping("/api/rooms/{roomId}/resources/my")
    public ResponseEntity<List<ResourceResponse>> getMyUploads(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                resourceService.getMyUploads(roomId, currentUser));
    }

    // ─── DELETE /api/rooms/{roomId}/resources/{resourceId} ────
    @Operation(summary = "Delete a resource (uploader or room owner only)")
    @DeleteMapping("/api/rooms/{roomId}/resources/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long roomId,
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User currentUser) {

        resourceService.deleteResource(resourceId, currentUser);
        return ResponseEntity.noContent().build();
    }
}