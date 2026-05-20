package com.example.inventory.service;

import com.example.inventory.client.ProductClient;
import com.example.inventory.dto.InventoryDto;
import com.example.inventory.exceptions.APIException;
import com.example.inventory.exceptions.ResourceNotFoundException;
import com.example.inventory.model.Inventory;
import com.example.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {

	@Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductClient productClient;

    public InventoryDto addStock(Long productId, Integer quantity) {
        // 1️⃣ Verify product exists
        try {
            productClient.getProductById(productId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        // 2️⃣ Add stock
        Optional<Inventory> existing = inventoryRepository.findByProductId(productId);

        Inventory inventory = existing.orElseGet(() -> {
            Inventory inv = new Inventory();
            inv.setProductId(productId);
            inv.setQuantityAvailable(0);
            return inv;
        });

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
        Inventory saved = inventoryRepository.save(inventory);

        return new InventoryDto(saved.getProductId(), saved.getQuantityAvailable());
    }

    public InventoryDto reduceStock(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        if (inventory.getQuantityAvailable() < quantity) {
            throw new APIException("Insufficient stock for product ID: " + productId);
        }

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
        Inventory saved = inventoryRepository.save(inventory);

        return new InventoryDto(saved.getProductId(), saved.getQuantityAvailable());
    }

    public InventoryDto checkStockByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        return new InventoryDto(inventory.getProductId(), inventory.getQuantityAvailable());
    }

    public boolean isStockAvailable(Long productId, Integer requiredQuantity) {
        return inventoryRepository.findByProductId(productId)
                .map(inv -> inv.getQuantityAvailable() >= requiredQuantity)
                .orElse(false);
    }
}
