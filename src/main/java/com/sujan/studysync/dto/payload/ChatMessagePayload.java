package com.sujan.studysync.dto.payload;

// This is what Angular SENDS to the server when a user types a message
// "payload" = data carried inside a WebSocket frame
// It's NOT a response DTO — it's inbound data
// We use a record here too — it's just parsed from JSON
public record ChatMessagePayload(
        String content    // the text the user typed
) {}
