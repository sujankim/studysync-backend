package com.sujan.studysync.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

// Handles all interaction with the Cloudinary API
// Upload a file → get back a URL and public ID
// Delete a file → remove it from Cloudinary
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // ─── Upload a file ────────────────────────────────────────
    // Returns a map with "url" and "public_id"
    // folder = where to store in Cloudinary e.g. "studysync/rooms/5"
    public Map<String, String> upload(
            MultipartFile file,
            String folder) throws IOException {

        // ObjectUtils.asMap = Cloudinary's helper to build params
        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        // resource_type auto = Cloudinary detects file type
                        // Handles images, videos, PDFs, docs automatically
                        "resource_type", "auto",
                        // use_filename = keep original filename
                        "use_filename", true,
                        // unique_filename = add suffix to avoid conflicts
                        "unique_filename", true
                )
        );

        // Cloudinary returns many fields — we only need these two
        return Map.of(
                "url",       (String) result.get("secure_url"),
                "public_id", (String) result.get("public_id")
        );
    }

    // ─── Delete a file ────────────────────────────────────────
    // publicId = the ID Cloudinary gave us when we uploaded
    // resourceType = "image", "video", or "raw" (PDF/docs)
    public void delete(
            String publicId,
            String resourceType) {

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
            log.info("Deleted Cloudinary resource: {}", publicId);
        } catch (IOException e) {
            // Log but don't crash — the DB record is still deleted
            log.error("Failed to delete Cloudinary resource: {}",
                    publicId, e);
        }
    }

    // ─── Determine Cloudinary resource type ───────────────────
    // Cloudinary uses different resource_type for images vs other files
    // "image"  = JPG, PNG, GIF, WebP etc.
    // "video"  = MP4, MOV, AVI etc.
    // "raw"    = PDF, DOC, XLS, ZIP etc.
    public String getCloudinaryResourceType(String contentType) {
        if (contentType == null) return "raw";
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        return "raw";  // PDFs, docs, etc.
    }
}

