package com.sujan.studysync.controller;

import com.sujan.studysync.dto.request.StartSessionRequest;
import com.sujan.studysync.dto.response.StudySessionResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<StudySessionResponse> start(
            @RequestBody(required = false) StartSessionRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                sessionService.startSession(request, currentUser));
    }

    @PostMapping("/end")
    public ResponseEntity<StudySessionResponse> end(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(sessionService.endSession(currentUser));
    }

    @GetMapping("/active")
    public ResponseEntity<StudySessionResponse> active(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                sessionService.getActiveSession(currentUser));
    }
}

