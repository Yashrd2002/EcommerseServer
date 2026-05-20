package com.example.product.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.product.client.InventoryClient;
import com.example.product.exceptions.APIException;
import com.example.product.exceptions.ResourceNotFoundException;
import com.example.product.model.Product;
import com.example.product.model.WishlistItem;
import com.example.product.payload.ProductDTO;
import com.example.product.repository.ProductRepository;
import com.example.product.repository.WishlistRepository;

import jakarta.transaction.Transactional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryClient inventoryClient;
    
    public WishlistItem addToWishlist(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        boolean alreadyExists = wishlistRepository.findByUserIdAndProduct_ProductId(userId, productId).isPresent();
        if (alreadyExists) {
            throw new APIException("Product is already in your wishlist.");
        }

        WishlistItem item = new WishlistItem();
        item.setUserId(userId);
        item.setProduct(product);
        return wishlistRepository.save(item);
    }

    public List<ProductDTO> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(item -> {
                    Product product = item.getProduct();
                    ProductDTO dto = new ProductDTO();

                    dto.setProductId(product.getProductId());
                    dto.setProductName(product.getProductName());
                    dto.setImages(product.getImages());
                    dto.setDescription(product.getDescription());
                    dto.setPrice(product.getPrice());
                    dto.setDiscount(product.getDiscount());
                    dto.setSpecialPrice(product.getSpecialPrice());
                    dto.setCategoryId(product.getCategory().getCategoryId());
                    dto.setCategoryName(product.getCategory().getCategoryName());
                    dto.setSellerId(product.getSellerId());

                    // Calculate average rating
                    if (product.getRatings() != null && !product.getRatings().isEmpty()) {
                        double avg = product.getRatings().stream()
                                .mapToDouble(r -> r.getRatingValue())
                                .average()
                                .orElse(0.0);
                        dto.setAverageRating(avg);
                    } else {
                        dto.setAverageRating(0.0);
                    }

                    // Optional: fetch stock from inventory service
                    try {
                        dto.setStock(inventoryClient.checkStock(product.getProductId()).getQuantityAvailable());
                    } catch (Exception e) {
                        dto.setStock(0);
                    }

                    return dto;
                })
                .toList();
    }


    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        boolean exists = wishlistRepository.findByUserIdAndProduct_ProductId(userId, productId).isPresent();
        if (!exists) {
            throw new APIException("Product is not in your wishlist.");
        }
        wishlistRepository.deleteByUserIdAndProduct_ProductId(userId, productId);
    }

    
    public boolean isProductWishlisted(Long userId, Long productId) {
        return wishlistRepository.findByUserIdAndProduct_ProductId(userId, productId).isPresent();
    }
}