package com.sujan.studysync.service;

import com.sujan.studysync.dto.request.InitiatePaymentRequest;
import com.sujan.studysync.dto.response.PaymentInitiateResponse;
import com.sujan.studysync.dto.response.SubscriptionResponse;
import com.sujan.studysync.model.User;

public interface SubscriptionService {

    // Step 1: Start the payment process
    // Returns Khalti's payment URL so Angular can redirect there
    PaymentInitiateResponse initiatePayment(
            InitiatePaymentRequest request,
            User currentUser);

    // Step 2: Called when Khalti redirects back
    // Verifies with Khalti, upgrades user if successful
    // Returns redirect URL (success or failure page)
    String handleCallback(String pidx, String status);

    // Get current subscription status
    SubscriptionResponse getSubscription(User currentUser);

    // Cancel Pro plan (sets to expire at period end)
    void cancelSubscription(User currentUser);
}
