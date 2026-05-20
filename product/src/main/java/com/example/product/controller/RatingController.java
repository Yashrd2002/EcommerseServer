 package com.example.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.product.model.Rating;
import com.example.product.services.RatingService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @PostMapping("/user/rate/{productId}")
    public ResponseEntity<Rating> addRating(@PathVariable Long productId,
                                            @RequestHeader("X-User-Id") Long userId,
                                            @RequestBody Map<String, Object> payload) {
        Double ratingValue = Double.valueOf(payload.get("ratingValue").toString());
        String review = (String) payload.getOrDefault("review", "");

        Rating rating = ratingService.addRating(productId, userId, ratingValue, review);
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/public/ratings/{productId}")
    public ResponseEntity<List<Rating>> getRatings(@PathVariable Long productId) {
        return ResponseEntity.ok(ratingService.getRatingsForProduct(productId));
    }

    // ✅ New route to check if user already rated this product
    @GetMapping("/user/check-rating/{productId}")
    public ResponseEntity<Map<String, Object>> checkUserRating(
            @PathVariable Long productId,
            @RequestHeader("X-User-Id") Long userId) {

        Rating rating = ratingService.getUserRatingForProduct(productId, userId);

        if (rating == null) {
            // user has NOT rated — safely return a response without causing NPE
            return ResponseEntity.ok(Map.of(
                    "productId", productId,
                    "userId", userId,
                    "hasRated", false,
                    "rating", Map.of() // or "rating": null
            ));
        }

        // user HAS rated
        return ResponseEntity.ok(Map.of(
                "productId", productId,
                "userId", userId,
                "hasRated", true,
                "rating", Map.of(
                        "ratingId", rating.getId(),
                        "ratingValue", rating.getRatingValue(),
                        "review", rating.getReview(),
                        "createdAt", rating.getCreatedAt()
                )
        ));
    }


}
