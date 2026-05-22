package com.sujan.studysync.config;

import com.sujan.studysync.enums.Plan;
import com.sujan.studysync.enums.UserRole;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.UserRepository;
import com.sujan.studysync.security.JwtService;
import com.sujan.studysync.service.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

// Called by Spring Security AFTER Google successfully authenticates the user
// This is where we:
// 1. Find or create the user in our database
// 2. Generate our own JWT tokens
// 3. Set the refresh cookie
// 4. Redirect to Angular with the access token in the URL
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService     jwtService;
    private final AppProperties  appProperties;
    private final EmailService   emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest  request,
            HttpServletResponse response,
            Authentication      authentication) throws IOException {

        // authentication.getPrincipal() = the Google user info
        // Spring Security already verified the Google token
        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        String email     = oauth2User.getAttribute("email");
        String name      = oauth2User.getAttribute("name");
        String avatarUrl = oauth2User.getAttribute("picture");
        String googleId  = oauth2User.getAttribute("sub"); // Google's user ID

        if (email == null) {
            log.error("OAuth2 user has no email");
            response.sendRedirect(
                    frontendUrl + "/login?error=no_email");
            return;
        }

        // Find existing user OR create a new one
        boolean isNewUser = !userRepository.existsByEmail(
                email.toLowerCase());

        User user = userRepository
                .findByEmail(email.toLowerCase())
                .orElseGet(() -> {
                    // New user signing in with Google for first time
                    // Generate username from email (before the @)
                    String baseUsername = email.split("@")[0]
                            .replaceAll("[^a-zA-Z0-9_]", "_")
                            .toLowerCase();

                    // Ensure username is unique — append number if taken
                    String username = baseUsername;
                    int    counter  = 1;
                    while (userRepository.existsByUsername(username)) {
                        username = baseUsername + counter++;
                    }

                    return User.builder()
                            .email(email.toLowerCase())
                            .name(name != null ? name : "StudySync User")
                            .username(username)
                            .avatarUrl(avatarUrl)
                            .role(UserRole.ROLE_USER)
                            .plan(Plan.FREE)
                            .provider("google")
                            .providerId(googleId)
                            // No password — OAuth2 users log in via Google
                            .build();
                });

        // Update avatar if changed on Google
        if (avatarUrl != null && !avatarUrl.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(avatarUrl);
        }

        User savedUser = userRepository.save(user);

        // Send welcome email to new OAuth2 users (non-blocking)
        if (isNewUser) {
            emailService.sendWelcomeEmail(savedUser);
        }

        // Generate our JWT tokens
        String accessToken  = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        // Set HttpOnly refresh token cookie (same as regular login)
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (
                appProperties.getJwt().getRefreshTokenExpiry() / 1000));
        response.addCookie(cookie);

        // Redirect to Angular callback page with access token in URL
        // Angular reads it, stores in memory signal, navigates to /dashboard
        String redirectUrl = frontendUrl
                + "/auth/callback?token="
                + accessToken;

        log.info("OAuth2 success for: {}", email);
        response.sendRedirect(redirectUrl);
    }
}
