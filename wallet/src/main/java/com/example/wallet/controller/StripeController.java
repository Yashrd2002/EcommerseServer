package com.example.wallet.controller;

import com.example.wallet.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wallet/stripe")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    // ✅ 1. Create PaymentIntent (called by frontend)
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestParam double amount) throws StripeException {
        Map<String, Object> response = stripeService.createPaymentIntent(amount, "inr");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestParam String paymentIntentId) {
        Map<String, Object> response = stripeService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> refundPayment(@RequestParam String paymentIntentId,
                                                             @RequestParam(required = false) Double amount)
            throws StripeException {
        Map<String, Object> response = stripeService.refundPayment(paymentIntentId, amount);
        return ResponseEntity.ok(response);
    }

}
