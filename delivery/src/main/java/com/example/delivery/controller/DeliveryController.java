package com.example.delivery.controller;

import com.example.delivery.dto.DeliveryResponseDto;
import com.example.delivery.model.DeliveryStatus;
import com.example.delivery.services.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    // Seller creates delivery from order
    @PostMapping("/seller/createFromOrder/{orderId}")
    public ResponseEntity<DeliveryResponseDto> createDeliveryFromOrder(
            @RequestHeader("X-User-Id") Long sellerId,
            @PathVariable Long orderId,
            @RequestParam String courierName) {

        DeliveryResponseDto delivery = deliveryService.createDeliveryFromOrder(sellerId, orderId, courierName);
        return ResponseEntity.ok(delivery);
    }
    
    @GetMapping("/seller/getAll")
    public ResponseEntity<List<DeliveryResponseDto>> getBySellerDeliveries(@RequestHeader("X-User-Id") Long sellerId) {
        List<DeliveryResponseDto> deliveries = deliveryService.getBySellerDeliveries(sellerId);
        return ResponseEntity.ok(deliveries);
    }
    
    

    // Admin - get all deliveries
    @GetMapping("/admin/getAll")
    public ResponseEntity<List<DeliveryResponseDto>> getAllDeliveries() {
        List<DeliveryResponseDto> deliveries = deliveryService.getAllDeliveries();
        return ResponseEntity.ok(deliveries);
    }

    // Admin - get delivery by ID
    @GetMapping("/admin/get/{deliveryId}")
    public ResponseEntity<DeliveryResponseDto> getDeliveryById(@PathVariable Long deliveryId) {
        DeliveryResponseDto delivery = deliveryService.getDeliveryById(deliveryId);
        return ResponseEntity.ok(delivery);
    }

    // Optional: User - track own delivery
    @GetMapping("/user/get/{orderId}")
    public ResponseEntity<DeliveryResponseDto> getUserDeliveryByOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {

        DeliveryResponseDto delivery = deliveryService.getDeliveryByUserAndOrder(userId, orderId);
        return ResponseEntity.ok(delivery);
    }
    
    @PatchMapping("/seller/updateStatus/{deliveryId}")
    public ResponseEntity<DeliveryResponseDto> updateDeliveryStatus(
            @RequestHeader("X-User-Id") Long sellerId,
            @PathVariable Long deliveryId,
            @RequestParam("status") DeliveryStatus status) {

        DeliveryResponseDto updatedDelivery = deliveryService.updateDeliveryStatus(sellerId, deliveryId, status);
        return ResponseEntity.ok(updatedDelivery);
    }

}
