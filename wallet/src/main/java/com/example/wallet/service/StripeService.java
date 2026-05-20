package com.example.wallet.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {


    public Map<String, Object> createPaymentIntent(double amount, String currency) throws StripeException {
        long amountInPaise = (long) (amount * 100);

        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInPaise);
        params.put("currency", currency);
        params.put("automatic_payment_methods", Map.of("enabled", true));

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("paymentIntentId", intent.getId());
        return response;
    }
    
    public Map<String, Object> confirmPayment(String paymentIntentId) {
        try {
            // Retrieve the existing intent
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            // If already confirmed, return it as success
            if ("succeeded".equals(intent.getStatus())) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "succeeded");
                response.put("paymentIntentId", intent.getId());
                response.put("amount", intent.getAmount() / 100.0);
                return response;
            }

            // Confirm the intent if not already done
            Map<String, Object> params = new HashMap<>();
            PaymentIntent confirmed = intent.confirm(params);

            Map<String, Object> response = new HashMap<>();
            response.put("status", confirmed.getStatus());
            response.put("paymentIntentId", confirmed.getId());
            response.put("amount", confirmed.getAmount() / 100.0);
            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Stripe confirmation failed: " + e.getMessage(), e);
        }
    }
    public Map<String, Object> refundPayment(String paymentIntentId, Double amount) throws StripeException {
        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId);

        // Partial refund if amount is provided
        if (amount != null && amount > 0) {
            long amountInPaise = (long) (amount * 100);
            paramsBuilder.setAmount(amountInPaise);
        }

        Refund refund = Refund.create(paramsBuilder.build());

        Map<String, Object> response = new HashMap<>();
        response.put("refundId", refund.getId());
        response.put("status", refund.getStatus());
        response.put("amount", refund.getAmount() / 100.0);
        response.put("paymentIntentId", refund.getPaymentIntent());
        return response;
    }
}
