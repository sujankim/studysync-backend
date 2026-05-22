package com.sujan.studysync.service;

import com.sujan.studysync.config.KhaltiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

// Handles all HTTP communication with Khalti's API
// Two operations:
// 1. initiate() → create payment session → get payment URL
// 2. verify()   → confirm payment was actually made
@Slf4j
@Service
@RequiredArgsConstructor
public class KhaltiService {

    private final KhaltiConfig khaltiConfig;

    // RestTemplate = Spring's HTTP client
    // We use it to call Khalti's JSON API
    private final RestTemplate restTemplate = new RestTemplate();

    // ─── Initiate Payment ─────────────────────────────────────
    // Calls Khalti to create a payment session
    // Returns the payment URL and pidx token
    @SuppressWarnings("unchecked")
    public Map<String, Object> initiatePayment(
            String orderId,        // our internal ID
            int    amountPaisa,    // 100 NPR = 10000 paisa
            String orderName,      // "StudySync Pro Plan"
            String customerName,
            String customerEmail,
            String customerPhone) {

        // Build request body — exactly what Khalti's API expects
        Map<String, Object> body = new HashMap<>();
        body.put("return_url",           khaltiConfig.getReturnUrl());
        body.put("website_url",          khaltiConfig.getWebsiteUrl());
        body.put("amount",               amountPaisa);
        body.put("purchase_order_id",    orderId);
        body.put("purchase_order_name",  orderName);

        // Customer info shown on Khalti's payment page
        Map<String, String> customerInfo = new HashMap<>();
        customerInfo.put("name",  customerName);
        customerInfo.put("email", customerEmail);
        customerInfo.put("phone", customerPhone != null
                ? customerPhone : "9800000000"); // fallback for OAuth2 users
        body.put("customer_info", customerInfo);

        // Set Khalti authorization header
        // Format: "key YOUR_SECRET_KEY"
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization",
                "key " + khaltiConfig.getSecretKey());

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    khaltiConfig.getInitiateUrl(),
                    entity,
                    Map.class
            );

            log.info("Khalti initiate response: {}",
                    response.getStatusCode());

            return response.getBody();

        } catch (Exception e) {
            log.error("Khalti initiate error: {}", e.getMessage());
            throw new RuntimeException(
                    "Payment gateway error. Please try again.");
        }
    }

    // ─── Verify Payment ───────────────────────────────────────
    // After user pays, Khalti redirects to our return_url with pidx
    // We MUST verify with Khalti that the payment actually succeeded
    // Never trust the redirect params alone — always verify server-side
    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyPayment(String pidx) {

        Map<String, String> body = new HashMap<>();
        body.put("pidx", pidx);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization",
                "key " + khaltiConfig.getSecretKey());

        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    khaltiConfig.getLookupUrl(),
                    entity,
                    Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Khalti verify error: {}", e.getMessage());
            throw new RuntimeException(
                    "Payment verification failed.");
        }
    }

    // ─── Check if payment status is Completed ─────────────────
    // Khalti returns status as "Completed", "Pending", "Refunded" etc.
    public boolean isPaymentCompleted(Map<String, Object> response) {
        if (response == null) return false;
        return "Completed".equals(response.get("status"));
    }
}
