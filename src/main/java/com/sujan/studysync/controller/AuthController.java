package com.sujan.studysync.controller;

import com.sujan.studysync.dto.request.LoginRequest;
import com.sujan.studysync.dto.request.RegisterRequest;
import com.sujan.studysync.dto.response.AuthResponse;
import com.sujan.studysync.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @Tag groups all endpoints in this controller under "Authentication" in Swagger UI
@Tag(name = "Authentication",
        description = "Register, login, token refresh, logout")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary     = "Register a new user",
            description = "Creates a new account. Returns JWT access token + sets refresh cookie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request, response));
    }

    @Operation(
            summary     = "Login with email and password",
            description = "Returns JWT access token + sets HttpOnly refresh cookie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        return ResponseEntity.ok(authService.login(request, response));
    }

    @Operation(
            summary     = "Refresh access token",
            description = "Uses the HttpOnly refresh cookie to issue a new access token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed"),
            @ApiResponse(responseCode = "401", description = "Refresh token missing or expired")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest  request,
            HttpServletResponse response) {

        return ResponseEntity.ok(authService.refresh(request, response));
    }

    @Operation(
            summary     = "Logout",
            description = "Clears the refresh token cookie."
    )
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.noContent().build();
    }
}
