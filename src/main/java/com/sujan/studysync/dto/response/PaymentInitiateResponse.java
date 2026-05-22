package com.sujan.studysync.dto.response;

// What we send back to Angular after initiating with Khalti
// Angular uses paymentUrl to redirect the user to Khalti
public record PaymentInitiateResponse(
        String paymentUrl,     // Khalti's checkout URL
        String pidx,           // Khalti's payment token
        Integer amountNpr,     // NPR 100
        String plan            // "PRO"
) {}
