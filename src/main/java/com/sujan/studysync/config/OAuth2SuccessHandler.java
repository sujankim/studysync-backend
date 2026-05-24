package com.sujan.studysync.config;

import com.sujan.studysync.model.User;
import com.sujan.studysync.security.JwtService;
import com.sujan.studysync.security.oauth.OAuth2UserInfo;
import com.sujan.studysync.security.oauth.OAuth2UserInfoFactory;
import com.sujan.studysync.service.EmailService;
import com.sujan.studysync.service.OAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2UserService oAuth2UserService;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        String registrationId =
                oauthToken.getAuthorizedClientRegistrationId();

        OAuth2UserInfo userInfo =
                OAuth2UserInfoFactory.getOAuth2UserInfo(
                        registrationId,
                        oauth2User.getAttributes()
                );

        boolean userAlreadyExists =
                oAuth2UserService.existsByEmail(userInfo.getEmail());

        User user =
                oAuth2UserService.processOAuthUser(userInfo);

        // Send welcome email only for new users
        if (!userAlreadyExists) {
            try {
                emailService.sendWelcomeEmail(user);
            } catch (Exception e) {
                log.error("Welcome email failed: {}", e.getMessage());
            }
        }

        // Generate JWTs
        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                jwtService.generateRefreshToken(user);

        /*
         * ENTERPRISE-GRADE REFRESH COOKIE
         *
         * SameSite=None is REQUIRED for:
         * Frontend (Vercel)
         * Backend (Render)
         *
         * Without this:
         * browser blocks the cookie silently
         */
        ResponseCookie refreshCookie =
                ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("None")
                        .path("/api/auth")
                        .maxAge(
                                appProperties
                                        .getJwt()
                                        .getRefreshTokenExpiry() / 1000
                        )
                        .build();

        response.addHeader(
                "Set-Cookie",
                refreshCookie.toString()
        );

        log.info(
                "OAuth2 login success provider={} email={}",
                userInfo.getProvider(),
                user.getEmail()
        );

        // Redirect frontend with access token
        String redirectUrl =
                frontendUrl + "/auth/callback?token=" + accessToken;

        response.sendRedirect(redirectUrl);
    }
}