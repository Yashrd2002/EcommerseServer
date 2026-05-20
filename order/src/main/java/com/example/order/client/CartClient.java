package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.order.payload.CartResponse;

@FeignClient(name = "cart-service", url = "http://localhost:8080/cart", configuration = FeignClientConfig.class)
public interface CartClient {
    @GetMapping("/user/getCart")
    CartResponse getCart(@RequestHeader("X-User-Id") Long userId);

    @DeleteMapping("/user/clear")
    void clearCart(@RequestHeader("X-User-Id") Long userId);
}