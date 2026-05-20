package com.example.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.product.model.WishlistItem;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserId(Long userId);
    Optional<WishlistItem> findByUserIdAndProduct_ProductId(Long userId, Long productId);
    void deleteByUserIdAndProduct_ProductId(Long userId, Long productId);
}
