package com.example.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<Map<String, Object>> authFallback() {
        return createFallbackResponse("Authentication service temporarily unavailable");
    }

    @GetMapping("/products")
    @PostMapping("/products")
    public Mono<Map<String, Object>> productsFallback() {
        return createFallbackResponse("Product service temporarily unavailable");
    }

    @GetMapping("/cart")
    @PostMapping("/cart")
    public Mono<Map<String, Object>> cartFallback() {
        return createFallbackResponse("Cart service temporarily unavailable");
    }

    @GetMapping("/orders")
    @PostMapping("/orders")
    public Mono<Map<String, Object>> ordersFallback() {
        return createFallbackResponse("Order service temporarily unavailable");
    }

    @GetMapping("/wallet")
    @PostMapping("/wallet")
    public Mono<Map<String, Object>> walletFallback() {
        return createFallbackResponse("Wallet service temporarily unavailable");
    }

    @GetMapping("/inventory")
    public Mono<Map<String, Object>> inventoryFallback() {
        return createFallbackResponse("Inventory service temporarily unavailable");
    }

    @GetMapping("/delivery")
    @PostMapping("/delivery")
    public Mono<Map<String, Object>> deliveryFallback() {
        return createFallbackResponse("Delivery service temporarily unavailable");
    }

    @GetMapping("/notifications")
    @PostMapping("/notifications")
    public Mono<Map<String, Object>> notificationsFallback() {
        return createFallbackResponse("Notification service temporarily unavailable");
    }

    private Mono<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        response.put("fallback", true);
        
        return Mono.just(response);
    }
}