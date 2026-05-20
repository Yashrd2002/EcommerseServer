package com.example.product.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.product.model.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByProduct_ProductId(Long productId);
    boolean existsByProduct_ProductIdAndUserId(Long productId, Long userId);
    
    Rating findByProduct_ProductIdAndUserId(Long productId, Long userId);
}
