package com.sujan.studysync.service.impl;

import com.sujan.studysync.dto.request.InitiatePaymentRequest;
import com.sujan.studysync.dto.response.PaymentInitiateResponse;
import com.sujan.studysync.dto.response.SubscriptionResponse;
import com.sujan.studysync.enums.Plan;
import com.sujan.studysync.model.Subscription;
import com.sujan.studysync.model.User;
import com.sujan.studysync.repository.SubscriptionRepository;
import com.sujan.studysync.repository.UserRepository;
import com.sujan.studysync.service.KhaltiService;
import com.sujan.studysync.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final KhaltiService khaltiService;

    // Pro plan price
    private static final int PRO_PRICE_NPR   = 100;
    private static final int PRO_PRICE_PAISA =
            PRO_PRICE_NPR * 100;  // 10000 paisa

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ─── Step 1: Initiate Payment ─────────────────────────────
    @Override
    @Transactional
    public PaymentInitiateResponse initiatePayment(
            InitiatePaymentRequest request,
            User currentUser) {

        // Generate a unique order ID for this payment attempt
        // UUID ensures no duplicate orders
        String orderId = "ss-" + UUID.randomUUID()
                .toString().replace("-", "")
                .substring(0, 16);

        // Call Khalti to create a payment session
        Map<String, Object> khaltiResponse  =
                khaltiService.initiatePayment(
                        orderId,
                        PRO_PRICE_PAISA,
                        "StudySync Pro Plan - 1 Month",
                        currentUser.getName(),
                        currentUser.getEmail(),
                        null // phone number is optional
                );

        // Extract the pidx (Khalti's payment token)
        // and the URL to redirect the user to
        String pidx       = (String) khaltiResponse.get("pidx");
        String paymentUrl = (String) khaltiResponse.get("payment_url");

        if (pidx == null || paymentUrl == null) {
            throw new RuntimeException(
                    "Invalid response from payment gateway.");
        }

        // Save a PENDING subscription record
        // Status = "pending" until Khalti confirms payment
        Subscription subscription = subscriptionRepository
                .findByUser(currentUser)
                .orElseGet(() -> Subscription.builder()
                        .user(currentUser)
                        .build());

        subscription.setKhaltiPidx(pidx);
        subscription.setAmountNpr(PRO_PRICE_NPR);
        subscription.setAmountPaisa(PRO_PRICE_PAISA);
        subscription.setStatus("pending");
        subscription.setPlan("PRO");

        subscriptionRepository.save(subscription);

        log.info("Payment initiated for user {} — pidx: {}",
                currentUser.getEmail(), pidx);

        return new PaymentInitiateResponse(
                paymentUrl,
                pidx,
                PRO_PRICE_NPR,
                "PRO"
        );
    }

    // ─── Step 2: Handle Callback ──────────────────────────────
    // Called when Khalti redirects the user back to our backend
    // URL looks like:
    // /api/payment/callback?pidx=xxx&status=Completed&...
    @Override
    @Transactional
    public String handleCallback(String pidx, String status) {

        // Step 1: Verify with Khalti FIRST (external HTTP call)
        // This is done before loading from DB to avoid session issues
        Map<String, Object> verification =
                khaltiService.verifyPayment(pidx);

        if (!khaltiService.isPaymentCompleted(verification)) {
            log.warn("Payment not completed for pidx: {}", pidx);
            return frontendUrl + "/billing/cancel?reason=not_completed";
        }

        // Step 2: Load subscription WITH user eagerly (JOIN FETCH)
        Subscription subscription = subscriptionRepository
                .findByKhaltiPidxWithUser(pidx)   // ← changed
                .orElse(null);

        if (subscription == null) {
            log.error("No subscription found for pidx: {}", pidx);
            return frontendUrl + "/billing/cancel?reason=not_found";
        }

        // Step 3: Activate subscription
        LocalDate now     = LocalDate.now();
        LocalDate endDate = now.plusDays(30);

        subscription.setStatus("active");
        subscription.setPeriodStart(now);
        subscription.setPeriodEnd(endDate);
        subscription.setRenewalEmailSent(false);
        subscriptionRepository.save(subscription);

        // Step 4: Upgrade user to PRO
        // user is now available because of JOIN FETCH above
        User user = subscription.getUser();
        user.setPlan(Plan.PRO);
        userRepository.save(user);

        log.info("User {} upgraded to PRO. Expires: {}",
                user.getEmail(), endDate);

        return frontendUrl + "/billing/success";
    }

    // ─── Get Subscription ─────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(User currentUser) {

        return subscriptionRepository
                .findByUser(currentUser)
                .map(sub -> new SubscriptionResponse(
                        sub.getPlan(),
                        sub.getStatus(),
                        sub.getAmountNpr(),
                        sub.getPeriodStart(),
                        sub.getPeriodEnd(),
                        "active".equals(sub.getStatus())
                ))
                .orElseGet(() -> new SubscriptionResponse(
                        "FREE",
                        "free",
                        null, null, null,
                        false
                ));
    }

    // ─── Cancel Subscription ──────────────────────────────────
    @Override
    @Transactional
    public void cancelSubscription(User currentUser) {

        subscriptionRepository
                .findByUser(currentUser)
                .ifPresent(sub -> {
                    // Don't immediately remove Pro access
                    // Let it expire at the end of the paid period
                    sub.setStatus("cancelled");
                    subscriptionRepository.save(sub);

                    log.info("Subscription cancelled for: {}",
                            currentUser.getEmail());
                });
    }
}