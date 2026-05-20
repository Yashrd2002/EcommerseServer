package com.example.wallet.controller;

import com.example.wallet.model.Wallet;
import com.example.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // ✅ Get wallet details
    @GetMapping("/getWallet")
    public ResponseEntity<Wallet> getWallet(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(walletService.getWallet(userId));
    }

    // ✅ Create payment intent for adding money
    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Double amount,
            @RequestParam(defaultValue = "usd") String currency) {

        Map<String, Object> response = walletService.createPaymentIntent(userId, amount, currency);
        return ResponseEntity.ok(response);
    }

    // ✅ Confirm and add money after successful payment
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmAddMoney(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String paymentIntentId) {

        Map<String, Object> response = walletService.confirmAddMoney(userId, paymentIntentId);
        return ResponseEntity.ok(response);
    }

    // ✅ Remove money (for payments using wallet)
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> deductMoney(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Double amount) {

        Map<String, Object> response = walletService.deductMoney(userId, amount);
        return ResponseEntity.ok(response);
    }
}
