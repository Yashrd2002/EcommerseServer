package com.example.order.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import com.example.order.client.CartClient;
import com.example.order.client.InventoryClient;
import com.example.order.client.NotificationClient;
import com.example.order.client.UserClient;
import com.example.order.client.WalletClient;
import com.example.order.exceptions.APIException;
import com.example.order.exceptions.ResourceNotFoundException;
import com.example.order.model.Order;
import com.example.order.model.OrderItem;
import com.example.order.payload.*;
import com.example.order.repository.OrderRepository;

import feign.FeignException;
import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartClient cartClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private WalletClient walletClient;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private NotificationClient notificationClient;
    
    // 🟢 Place Order
    @Transactional
    @CircuitBreaker(name = "orderService", fallbackMethod = "placeOrderFallback")
    @Retry(name = "orderService")
    public OrderDto placeOrder(Long userId, Long addressId) {
        // 1️⃣ Validate user
        if (userId == null) {
            throw new APIException("User not found: " + userId);
        }

        // 2️⃣ Get all user addresses
        ResponseEntity<List<AddressResponse>> addressResponse = userClient.getAddresses(userId);
        List<AddressResponse> addresses = addressResponse.getBody();
        if (addresses == null || addresses.isEmpty()) {
            throw new APIException("No address found for user: " + userId);
        }

        // 3️⃣ Select address
        AddressResponse address = (addressId != null)
                ? addresses.stream()
                    .filter(a -> a.getId().equals(addressId))
                    .findFirst()
                    .orElseThrow(() -> new APIException("Address not found for ID: " + addressId))
                : addresses.get(0);

        // 4️⃣ Get cart
        CartResponse cart = cartClient.getCart(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new APIException("Cart is empty. Cannot place order.");
        }

        // 5️⃣ Check stock availability
        for (CartItemResponse cartItem : cart.getItems()) {
            try {
                InventoryDto stock = inventoryClient.checkStock(cartItem.getProductId());
                if (stock == null || stock.getQuantityAvailable() < cartItem.getQuantity()) {
                    throw new APIException("Insufficient stock for product: " + cartItem.getProductName());
                }
            } catch (FeignException.NotFound ex) {
                throw new APIException("Inventory not found for product: " + cartItem.getProductName());
            }
        }

        // 6️⃣ Create order (payment pending initially)
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus("PAYMENT_PENDING");
        order.setAddressId(address.getId());

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemResponse cartItem : cart.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(cartItem.getProductId());
            item.setProductName(cartItem.getProductName());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getPrice());
            item.setDiscount(cartItem.getSpecialPrice());
            item.setSellerId(cartItem.getSellerId());
            item.setOrder(order);
            totalAmount += cartItem.getQuantity() * cartItem.getSpecialPrice();
            orderItems.add(item);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // 7️⃣ Save order (status: PAYMENT_PENDING)
        Order savedOrder = orderRepository.save(order);

        // 8️⃣ Return DTO for frontend to initiate payment
        OrderDto responseDto = mapToDto(savedOrder, address);
        responseDto.setPaymentStatus("PENDING");
        responseDto.setMessage("Order created successfully. Proceed to payment.");

        return responseDto;
    }

    
    @Transactional
    public Map<String, Object> confirmPayment(Long userId, Long orderId, String method, String paymentIntentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new APIException("Order not found: " + orderId));

        if (!"PAYMENT_PENDING".equals(order.getOrderStatus())) {
            throw new APIException("Order already paid or invalid state");
        }

        double totalAmount = order.getTotalAmount();

        Map<String, Object> body;

        if ("wallet".equalsIgnoreCase(method)) {
            // 🏦 Deduct from wallet
            ResponseEntity<Map<String, Object>> walletResponse = walletClient.removeMoney(userId, totalAmount);
            body = walletResponse.getBody();

            if (body == null || !"success".equalsIgnoreCase(String.valueOf(body.get("status")))) {
                throw new APIException("Wallet deduction failed");
            }

            order.setPaymentMethod("WALLET");
            order.setOrderStatus("PLACED");

        } else if ("card".equalsIgnoreCase(method)) {
            // 💳 Confirm Stripe payment
            ResponseEntity<Map<String, Object>> confirmResponse = walletClient.confirmPayment(userId, paymentIntentId);
            body = confirmResponse.getBody();

            if (body == null || !"succeeded".equalsIgnoreCase(String.valueOf(body.get("status")))) {
                throw new APIException("Stripe payment failed: " + (body != null ? body.get("message") : "unknown error"));
            }

            order.setPaymentMethod("CARD");
            order.setOrderStatus("PLACED");

        } else {
            throw new APIException("Invalid payment method: " + method);
        }

        orderRepository.save(order);

        // ✅ After payment success: reduce stock and clear cart
        CartResponse cart = cartClient.getCart(userId);
        for (CartItemResponse cartItem : cart.getItems()) {
            inventoryClient.reduceStock(cartItem.getProductId(), cartItem.getQuantity());
        }
        cartClient.clearCart(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Payment successful and order confirmed");
        response.put("orderId", orderId);
        response.put("paymentMethod", method);
        response.put("status", "success");
        return response;
    }

    
    
    


    // 🟢 Get user orders
    public List<OrderDto> getOrdersByUserId(Long userId) {
        if (userId == null) {
            throw new ResourceNotFoundException("User", "userId", userId);
        }

        List<Order> orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("Orders", "userId", userId);
        }

        List<OrderDto> dtos = new ArrayList<>();
        for (Order order : orders) {
        	List<AddressResponse> addressList = userClient.getAddresses(order.getAddressId()).getBody();
        	AddressResponse address = (addressList != null && !addressList.isEmpty()) ? addressList.get(0) : null;
            dtos.add(mapToDto(order, address));
        }

        return dtos;
    }

    // 🟢 Get seller orders
    public List<OrderDto> getOrdersBySellerId(Long sellerId) {
        List<Order> orders = orderRepository.findOrdersBySellerId(sellerId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("Orders", "sellerId", sellerId);
        }

        List<OrderDto> dtos = new ArrayList<>();
        for (Order order : orders) {
        	AddressResponse address = userClient.getAddressesById(order.getAddressId()).getBody();

            dtos.add(mapToDto(order, address));
        }

        return dtos;
    }

    // 🟢 Get a single order by seller
    public OrderDto getOrderByIdForSeller(Long sellerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        boolean isSellerOrder = order.getOrderItems().stream()
                .anyMatch(item -> item.getSellerId().equals(sellerId));

        if (!isSellerOrder) {
            throw new APIException("You are not authorized to view this order");
        }

        AddressResponse address = userClient.getAddressesById(order.getAddressId()).getBody();

        return mapToDto(order, address);
    }

    // 🧩 Utility: Convert Entity -> DTO
    private OrderDto mapToDto(Order order, AddressResponse address) {
        List<OrderItemDto> items = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            items.add(new OrderItemDto(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getDiscount(),
                item.getSellerId()
            ));
        }

        OrderDto dto = new OrderDto(); // no-arg constructor
        dto.setId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod()); // may be null initially
        dto.setAddress(address);
        dto.setItems(items);
        // optional:
        // dto.setPaymentStatus(...);
        // dto.setMessage(...);

        return dto;
    }

    // Circuit Breaker Fallback Methods
    public OrderDto placeOrderFallback(Long userId, Long addressId, Exception ex) {
        OrderDto fallbackDto = new OrderDto();
        fallbackDto.setMessage("Order service temporarily unavailable. Please try again later.");
        fallbackDto.setPaymentStatus("FAILED");
        return fallbackDto;
    }

    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartFallback")
    public CartResponse getCartWithCircuitBreaker(Long userId) {
        return cartClient.getCart(userId);
    }

    public CartResponse getCartFallback(Long userId, Exception ex) {
        CartResponse fallback = new CartResponse();
        fallback.setItems(new ArrayList<>());
        return fallback;
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "checkStockFallback")
    public InventoryDto checkStockWithCircuitBreaker(Long productId) {
        return inventoryClient.checkStock(productId);
    }

    public InventoryDto checkStockFallback(Long productId, Exception ex) {
        InventoryDto fallback = new InventoryDto();
        fallback.setProductId(productId);
        fallback.setQuantityAvailable(0);
        return fallback;
    }

}
