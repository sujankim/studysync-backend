package com.sujan.studysync.controller;

import com.sujan.studysync.dto.request.StartSessionRequest;
import com.sujan.studysync.dto.response.StudySessionResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Study Sessions",
        description = "Start/end study sessions and track progress")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(
            summary     = "Start a study session",
            description = "Ends any existing active session then starts a new one."
    )
    @PostMapping("/start")
    public ResponseEntity<StudySessionResponse> start(
            @RequestBody(required = false) StartSessionRequest request,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                sessionService.startSession(request, currentUser));
    }

    @Operation(
            summary     = "End the current active session",
            description = "Calculates duration and updates streak."
    )
    @PostMapping("/end")
    public ResponseEntity<StudySessionResponse> end(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                sessionService.endSession(currentUser));
    }

    @Operation(summary = "Get the current active session if any")
    @GetMapping("/active")
    public ResponseEntity<StudySessionResponse> active(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                sessionService.getActiveSession(currentUser));
    }
}


