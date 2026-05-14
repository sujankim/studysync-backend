package com.sujan.studysync.exception;

import java.time.LocalDateTime;

// Sent to client when something goes wrong
// e.g. { "status": 404, "error": "Not Found", "message": "Room not found", ... }
public record ErrorResponse(
        int           status,
        String        error,
        String        message,
        String        path,
        LocalDateTime timestamp
) {
    // ─── Static factory method ───────────────────────────────
    // Usage: ErrorResponse.of(404, "Not Found", "Room not found", "/api/rooms/99")
    public static ErrorResponse of(
            int    status,
            String error,
            String message,
            String path) {
        return new ErrorResponse(status, error, message, path, LocalDateTime.now());
    }
}
