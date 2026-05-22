package com.sujan.studysync.controller;

import com.sujan.studysync.dto.request.InitiatePaymentRequest;
import com.sujan.studysync.dto.response.PaymentInitiateResponse;
import com.sujan.studysync.dto.response.SubscriptionResponse;
import com.sujan.studysync.model.User;
import com.sujan.studysync.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@Tag(name = "Payments",
        description = "Khalti payment integration and subscription management")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final SubscriptionService subscriptionService;

    // ─── Initiate Payment ─────────────────────────────────────
    // Angular calls this → gets Khalti payment URL
    @Operation(summary = "Initiate Khalti payment for Pro plan")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @RequestBody InitiatePaymentRequest request,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                subscriptionService.initiatePayment(
                        request, currentUser));
    }

    // ─── Khalti Callback ──────────────────────────────────────
    // Khalti redirects here after payment
    // NOT secured with JWT — Khalti doesn't know our user's token
    // We use the pidx to identify the payment instead
    @Operation(
            summary = "Khalti payment callback",
            description = "Called by Khalti after payment. " +
                    "Verifies and activates Pro plan."
    )
    @GetMapping("/callback")
    public void handleCallback(
            @RequestParam String pidx,
            @RequestParam(required = false,
                    defaultValue = "") String status,
            HttpServletResponse response) throws IOException {

        log.info("Khalti callback — pidx: {}, status: {}",
                pidx, status);

        // handleCallback returns the redirect URL
        // (success page or failure page on the frontend)
        String redirectUrl =
                subscriptionService.handleCallback(pidx, status);

        // Redirect browser to Angular page
        response.sendRedirect(redirectUrl);
    }

    // ─── Get Subscription ─────────────────────────────────────
    @Operation(summary = "Get current subscription status")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                subscriptionService.getSubscription(currentUser));
    }

    // ─── Cancel Subscription ──────────────────────────────────
    @Operation(summary = "Cancel Pro subscription at period end")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelSubscription(
            @AuthenticationPrincipal User currentUser) {

        subscriptionService.cancelSubscription(currentUser);
        return ResponseEntity.noContent().build();
    }
}
