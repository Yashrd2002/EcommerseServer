package com.example.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.cart.payload.ProductDTO;

@FeignClient(name = "product-service", url = "http://localhost:8082/products", configuration = FeignClientConfig.class)
public interface ProductClient {

    @GetMapping("/public/view/{productId}")
    ResponseEntity<ProductDTO> getProductById(@PathVariable("productId") Long productId);
}