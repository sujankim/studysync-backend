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
@RequestMapping("/api/rooms/{roomId}/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    // ─── Upload a File ────────────────────────────────────────
    // Uses multipart/form-data — different from JSON endpoints
    // Angular sends: the file + title + description as form fields
    @Operation(
            summary = "Upload a file to a room",
            description = "Supports PDF, images, videos, documents. Max 50MB."
    )
    @PostMapping(
            value    = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResourceResponse> uploadFile(
            @PathVariable Long roomId,
            // @RequestPart = one part of a multipart form
            @RequestPart("file")  MultipartFile file,
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

    // ─── Add a Link ───────────────────────────────────────────
    @Operation(summary = "Add a link resource to a room")
    @PostMapping("/link")
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

    // ─── Get All Resources ────────────────────────────────────
    @Operation(summary = "Get all resources in a room")
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getResources(
            @PathVariable Long roomId,
            @RequestParam(required = false) ResourceType type,
            @AuthenticationPrincipal User currentUser) {

        if (type != null) {
            return ResponseEntity.ok(
                    resourceService.getRoomResourcesByType(roomId, type));
        }

        return ResponseEntity.ok(
                resourceService.getRoomResources(roomId));
    }

    // ─── My Uploads ───────────────────────────────────────────
    @Operation(summary = "Get resources uploaded by the current user")
    @GetMapping("/my")
    public ResponseEntity<List<ResourceResponse>> getMyUploads(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                resourceService.getMyUploads(roomId, currentUser));
    }

    // ─── Delete Resource ──────────────────────────────────────
    @Operation(summary = "Delete a resource (uploader or owner only)")
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long roomId,
            @PathVariable Long resourceId,
            @AuthenticationPrincipal User currentUser) {

        resourceService.deleteResource(resourceId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
