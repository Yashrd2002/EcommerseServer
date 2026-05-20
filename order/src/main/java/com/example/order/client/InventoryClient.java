package com.example.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.order.payload.InventoryDto;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {

    @GetMapping("/inventory/public/check/{productId}")
    InventoryDto checkStock(@PathVariable("productId") Long productId);

    @PostMapping("/inventory/seller/reduceStock/{productId}")
    InventoryDto reduceStock(@PathVariable("productId") Long productId,
                             @RequestParam("quantity") Integer quantity);

}
