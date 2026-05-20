package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
    name = "wallet-service",
    url = "http://localhost:8080/wallet"
)
public interface WalletClient {

    @PostMapping("/remove")
    ResponseEntity<Map<String, Object>> removeMoney(
        @RequestHeader("X-User-Id") Long userId,
        @RequestParam("amount") Double amount
    );

    @PostMapping("/stripe/confirm")
    ResponseEntity<Map<String, Object>> confirmPayment(
        @RequestHeader("X-User-Id") Long userId,
        @RequestParam("paymentIntentId") String paymentIntentId
    );
}
