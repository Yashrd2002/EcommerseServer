package com.example.product.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.product.model.WishlistItem;
import com.example.product.payload.ProductDTO;
import com.example.product.services.WishlistService;

@RestController
@RequestMapping("/products/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PostMapping("/add/{productId}")
    public ResponseEntity<Map<String, Object>> addToWishlist(@PathVariable Long productId,
                                                      @RequestHeader("X-User-Id") Long userId) {
        WishlistItem item = wishlistService.addToWishlist(userId, productId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product added to wishlist successfully.");
        response.put("productId", productId);
        response.put("wishlistItemId", item.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view")
    public ResponseEntity<List<ProductDTO>> viewWishlist(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(wishlistService.getWishlist(userId));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Map<String, String>> removeFromWishlist(@PathVariable Long productId,
                                                                  @RequestHeader("X-User-Id") Long userId) {
        wishlistService.removeFromWishlist(userId, productId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product removed from wishlist successfully.");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> isWishlisted(@PathVariable Long productId,
                                                            @RequestHeader("X-User-Id") Long userId) {
        boolean isWishlisted = wishlistService.isProductWishlisted(userId, productId);
        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("wishlisted", isWishlisted);
        return ResponseEntity.ok(response);
    }
}