package com.example.inventory.client;

import com.example.inventory.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {

    @GetMapping("/products/public/view/{productId}")
    ProductDTO getProductById(@PathVariable("productId") Long productId);
}
