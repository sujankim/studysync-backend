package com.sujan.studysync.dto.request;

// What Angular sends to start the payment process
public record InitiatePaymentRequest(
        // Which plan they want (we only have "PRO" for now)
        String plan
) {}
