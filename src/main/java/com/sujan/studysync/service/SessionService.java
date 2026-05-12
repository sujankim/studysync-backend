package com.sujan.studysync.service;

import com.sujan.studysync.dto.request.StartSessionRequest;
import com.sujan.studysync.dto.response.StudySessionResponse;
import com.sujan.studysync.model.User;

public interface SessionService {
    StudySessionResponse startSession(StartSessionRequest request, User currentUser);
    StudySessionResponse endSession(User currentUser);
    StudySessionResponse getActiveSession(User currentUser);
}
