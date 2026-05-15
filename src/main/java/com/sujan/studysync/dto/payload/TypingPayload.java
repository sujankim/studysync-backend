package com.sujan.studysync.dto.payload;

// Sent by Angular when a user starts or stops typing
// isTyping = true  → "Sujan is typing..."
// isTyping = false → typing indicator disappears
public record TypingPayload(
        boolean isTyping
) {}
