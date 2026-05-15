package com.sujan.studysync.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// @EnableWebSocketMessageBroker = "turn on STOMP WebSocket support"
// WebSocketMessageBrokerConfigurer = "I want to customize the WebSocket settings"
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    // ─── Step 1: Register the WebSocket endpoint ──────────────
    // This is the URL Angular connects to first
    // SockJS = fallback if WebSocket not available (always use in prod)
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")           // Angular connects to /ws
                .setAllowedOrigins(
                        "http://localhost:4200",
                        frontendUrl)
                .withSockJS();               // fallback support
    }

    // ─── Step 2: Configure message routing ───────────────────
    // This defines WHERE messages go:
    // /app/**     = messages handled by @MessageMapping controllers
    // /topic/**   = broadcast to ALL subscribers of that topic
    // /user/**    = send to ONE specific user (private messages)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Messages going TO the server start with /app
        // e.g. Angular sends to /app/room/5/send
        registry.setApplicationDestinationPrefixes("/app");

        // Messages going FROM the server to clients:
        // /topic = broadcast (everyone in the room)
        // /user  = private (just one user — for notifications later)
        registry.enableSimpleBroker("/topic", "/user");

        // For private user messages, prefix the destination with /user
        registry.setUserDestinationPrefix("/user");
    }

    //  Register interceptor on the inbound channel
    // Every incoming WebSocket message passes through this first
    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}