package com.example.cart.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.cart.client.UserClient;
import com.example.cart.model.Cart;
import com.example.cart.payload.CartItemRequest;
import com.example.cart.payload.CartResponse;
import com.example.cart.service.CartService;
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    
    @GetMapping("/user/getCart")
    public CartResponse getCart(@RequestHeader("X-User-Id") Long userId) {
        return cartService.getCartResponse(userId);
    }

    @PostMapping("/user/add")
    public Cart addToCart(@RequestBody CartItemRequest request,@RequestHeader("X-User-Id") Long userId) {
        return cartService.addToCart(request,userId);
    }

    @DeleteMapping("/user/remove")
    public ResponseEntity<String> removeItem(@RequestHeader("X-User-Id") Long userId,
                                             @RequestParam Long productId) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.ok("Item removed");
    }

    @DeleteMapping("/user/clear")
    public ResponseEntity<String> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared");
    }
}