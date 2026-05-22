package com.sujan.studysync.dto.response;

import java.time.LocalDate;

// Current subscription status — shown on the billing page
public record SubscriptionResponse(
        String    plan,          // "FREE" or "PRO"
        String    status,        // "active", "expired", "pending"
        Integer   amountNpr,
        LocalDate periodStart,
        LocalDate periodEnd,
        Boolean   willRenew      // true = user can renew before expiry
) {}
