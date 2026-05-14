package com.sujan.studysync.service.impl;

import com.sujan.studysync.config.AppProperties;
import com.sujan.studysync.dto.request.LoginRequest;
import com.sujan.studysync.dto.request.RegisterRequest;
import com.sujan.studysync.dto.response.AuthResponse;
import com.sujan.studysync.dto.response.UserResponse;
import com.sujan.studysync.enums.Plan;
import com.sujan.studysync.enums.UserRole;
import com.sujan.studysync.exception.EmailAlreadyExistsException;
import com.sujan.studysync.exception.InvalidCredentialsException;
import com.sujan.studysync.exception.UserNotFoundException;
import com.sujan.studysync.exception.UsernameAlreadyExistsException;
import com.sujan.studysync.mapper.UserMapper;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.UserRepository;
import com.sujan.studysync.security.JwtService;
import com.sujan.studysync.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final UserMapper userMapper;

    // ─── Register ─────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        User user = User.builder()
                .name(request.name())
                .username(request.username().toLowerCase())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_USER)
                .plan(Plan.FREE)
                .provider("local")
                .build();

        userRepository.save(user);

        return buildAuthResponse(user, response);
    }

    // ─── Login ────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse login(
            LoginRequest request,
            HttpServletResponse response) {

        // .email() not .getEmail() — records use no-prefix getters
        User user = userRepository
                .findByEmail(request.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return buildAuthResponse(user, response);
    }

    // ─── Refresh Token ────────────────────────────────────────────
    @Override
    public AuthResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = null;

        if (request.getCookies() != null) {
            refreshToken = Arrays.stream(request.getCookies())
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (refreshToken == null || jwtService.isTokenExpired(refreshToken)) {
            throw new InvalidCredentialsException();
        }

        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new InvalidCredentialsException();
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return buildAuthResponse(user, response);
    }

    // ─── Logout ───────────────────────────────────────────────────
    @Override
    public void logout(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
    }

    // ─── Private Helpers ──────────────────────────────────────────

    private AuthResponse buildAuthResponse(
            User user, HttpServletResponse response) {

        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        setRefreshTokenCookie(response, refreshToken);

        return new AuthResponse(
                accessToken,
                userMapper.toResponse(user)  // ← mapper does the work
        );
    }

    private void setRefreshTokenCookie(
            HttpServletResponse response, String refreshToken) {

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (
                appProperties.getJwt().getRefreshTokenExpiry() / 1000));
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
