package com.example.cart.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.example.cart.payload.ProductDTO;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ResponseEntity<ProductDTO> getProductById(Long productId) {
        ProductDTO fallbackProduct = new ProductDTO();
        fallbackProduct.setProductId(productId);
        fallbackProduct.setProductName("Product Unavailable");
        fallbackProduct.setDescription("Service temporarily unavailable");
        fallbackProduct.setPrice(0.0);
        fallbackProduct.setSpecialPrice(0.0);
        
        return ResponseEntity.ok(fallbackProduct);
    }
}