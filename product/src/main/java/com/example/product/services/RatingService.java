package com.example.product.services;

import com.example.product.model.Rating;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import com.example.product.repository.RatingRepository;
import com.example.product.exceptions.APIException;
import com.example.product.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ProductRepository productRepository;

    // 🚫 Only add new rating (throw error if already rated)
    public Rating addRating(Long productId, Long userId, Double ratingValue, String review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (ratingRepository.existsByProduct_ProductIdAndUserId(productId, userId)) {
            throw new APIException("User has already rated this product. Use update-rating instead.");
        }

        Rating rating = Rating.builder()
                .product(product)
                .userId(userId)
                .ratingValue(ratingValue)
                .review(review)
                .createdAt(LocalDateTime.now())
                .build();

        ratingRepository.save(rating);

        recalculateAverage(productId, product);
        return rating;
    }

    public Rating getUserRatingForProduct(Long productId, Long userId) {
        return ratingRepository.findByProduct_ProductIdAndUserId(productId, userId);
    }

    // 📊 Helper: Recalculate average rating for product
    private void recalculateAverage(Long productId, Product product) {
        List<Rating> allRatings = ratingRepository.findByProduct_ProductId(productId);
        double avg = allRatings.stream()
                .mapToDouble(Rating::getRatingValue)
                .average()
                .orElse(0.0);

        product.setAverageRating(avg);
        productRepository.save(product);
    }

    public List<Rating> getRatingsForProduct(Long productId) {
        return ratingRepository.findByProduct_ProductId(productId);
    }
}
