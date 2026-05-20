package com.example.wallet.service;

import com.example.wallet.exceptions.APIException;
import com.example.wallet.model.Wallet;
import com.example.wallet.repository.WalletRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PaymentService paymentService;

    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(new Wallet(null, userId, 0.0)));
    }

    // ✅ Step 1: Create PaymentIntent (called before paying)
    public Map<String, Object> createPaymentIntent(Long userId, Double amount, String currency) {
        if (amount == null || amount <= 0) {
            throw new APIException("Amount must be greater than zero");
        }

        try {
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(amount, currency);
            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("amount", amount);
            response.put("currency", currency);
            response.put("status", "created");
            return response;
        } catch (StripeException e) {
            throw new APIException("Stripe error: " + e.getMessage());
        }
    }

 // ✅ Step 2: Verify payment and update wallet balance
    public Map<String, Object> confirmAddMoney(Long userId, String paymentIntentId) {
        try {
            // Retrieve existing payment intent (do NOT re-confirm)
            PaymentIntent confirmed = PaymentIntent.retrieve(paymentIntentId);

            if (!"succeeded".equals(confirmed.getStatus())) {
                throw new APIException("Payment not successful. Status: " + confirmed.getStatus());
            }

            double amountPaid = confirmed.getAmount() / 100.0;

            Wallet wallet = getWallet(userId);
            wallet.setBalance(wallet.getBalance() + amountPaid);
            walletRepository.save(wallet);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Wallet recharged successfully");
            response.put("newBalance", wallet.getBalance());
            response.put("amountAdded", amountPaid);
            response.put("status", true);
            return response;

        } catch (StripeException e) {
            throw new APIException("Stripe confirmation failed: " + e.getMessage());
        }
    }


    // ✅ Step 3: Deduct money for wallet-based payments
    public Map<String, Object> deductMoney(Long userId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new APIException("Amount must be greater than zero");
        }

        Wallet wallet = getWallet(userId);
        if (wallet.getBalance() < amount) {
            throw new APIException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Amount deducted successfully");
        response.put("remainingBalance", wallet.getBalance());
        response.put("status", "success");
        return response;
    }
}
