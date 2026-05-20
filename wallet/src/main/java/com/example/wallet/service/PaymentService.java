package com.example.wallet.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Service
public class PaymentService {

    public PaymentIntent createPaymentIntent(Double amount, String currency) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", (int)(amount * 100)); // Stripe uses cents
        params.put("currency", currency);
        params.put("payment_method_types", List.of("card"));

        return PaymentIntent.create(params);
    }
    public PaymentIntent confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        Map<String, Object> params = new HashMap<>();
        params.put("payment_method", "pm_card_visa");

        return paymentIntent.confirm(params);
    }
}