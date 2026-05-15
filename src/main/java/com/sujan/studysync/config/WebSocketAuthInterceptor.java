package com.sujan.studysync.config;

import com.sujan.studysync.repository.UserRepository;
import com.sujan.studysync.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// This interceptor checks the JWT token when a WebSocket CONNECTS
// Without this, anyone could connect to the WebSocket without logging in
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        // Only authenticate on CONNECT — not every message
        if (accessor != null
                && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Angular sends the JWT in the Authorization header
            // Header format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
            String authHeader = accessor
                    .getFirstNativeHeader("Authorization");

            if (authHeader != null
                    && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                try {
                    String email = jwtService.extractEmail(token);

                    userRepository.findByEmail(email).ifPresent(user -> {
                        if (jwtService.isTokenValid(token, user)) {
                            // Attach the authenticated user to this WebSocket session
                            // Spring will know WHO sent each message
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            user, null, user.getAuthorities());

                            accessor.setUser(auth);
                            SecurityContextHolder.getContext()
                                    .setAuthentication(auth);
                        }
                    });
                } catch (Exception ignored) {
                    // Invalid token — connection will be rejected
                    // by Spring Security downstream
                }
            }
        }

        return message;
    }
}