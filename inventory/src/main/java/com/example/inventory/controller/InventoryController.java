package com.example.inventory.controller;

import com.example.inventory.dto.InventoryDto;
import com.example.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/seller/addStock/{productId}")
    public ResponseEntity<InventoryDto> addStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.addStock(productId, quantity));
    }

    @PostMapping("/seller/reduceStock/{productId}")
    public ResponseEntity<InventoryDto> reduceStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.reduceStock(productId, quantity));
    }

    @GetMapping("/public/check/{productId}")
    public ResponseEntity<InventoryDto> checkStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.checkStockByProductId(productId));
    }
}
