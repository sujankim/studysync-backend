package com.sujan.studysync.service;

import com.sujan.studysync.dto.request.LoginRequest;
import com.sujan.studysync.dto.request.RegisterRequest;
import com.sujan.studysync.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request, HttpServletResponse response);

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse refresh(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletResponse response);
}
