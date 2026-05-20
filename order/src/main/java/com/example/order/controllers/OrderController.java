package com.example.order.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.order.model.AddressIdRequest;
import com.example.order.payload.OrderDto;
import com.example.order.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 🟢 User places order
    @PostMapping("/user/place")
    public ResponseEntity<OrderDto> placeOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddressIdRequest request) {

        OrderDto order = orderService.placeOrder(userId, request.getAddressId());
        return ResponseEntity.ok(order);
    }


    @PostMapping("/user/confirm-payment")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long orderId,
            @RequestParam String method,  // wallet or card
            @RequestParam(required = false) String paymentIntentId) {

        Map<String, Object> response = orderService.confirmPayment(userId, orderId, method, paymentIntentId);
        return ResponseEntity.ok(response);
    }

    // 🟢 User gets all their orders
    @GetMapping("/user/getOrder")
    public ResponseEntity<List<OrderDto>> getOrdersByUser(@RequestHeader("X-User-Id") Long userId) {
        List<OrderDto> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // 🟢 Seller gets a single order details
    @GetMapping("/seller/getOrder/{orderId}")
    public ResponseEntity<OrderDto> getOrderByIdForSeller(
            @RequestHeader("X-User-Id") Long sellerId,
            @PathVariable Long orderId) {
        OrderDto order = orderService.getOrderByIdForSeller(sellerId, orderId);
        return ResponseEntity.ok(order);
    }

    // 🟢 Seller gets all their orders
    @GetMapping("/seller/getOrders")
    public ResponseEntity<List<OrderDto>> getOrdersBySeller(@RequestHeader("X-User-Id") Long sellerId) {
        List<OrderDto> orders = orderService.getOrdersBySellerId(sellerId);
        return ResponseEntity.ok(orders);
    }

    // 🟢 Admin gets orders by seller ID
    @GetMapping("/admin/getOrderBySellerId/{sellerId}")
    public ResponseEntity<List<OrderDto>> getOrdersBySellerId(@PathVariable("sellerId") Long sellerId) {
        List<OrderDto> orders = orderService.getOrdersBySellerId(sellerId);
        return ResponseEntity.ok(orders);
    }
}
