package com.example.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.product.payload.InventoryDto;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {

    @GetMapping("/inventory/public/check/{productId}")
    InventoryDto checkStock(@PathVariable("productId") Long productId);

}
