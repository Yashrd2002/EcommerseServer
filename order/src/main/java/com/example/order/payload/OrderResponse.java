package com.example.order.payload;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class OrderResponse {
    private Long orderId;
    private String email;
    private LocalDate orderDate;
    private Double totalAmount;
    private String orderStatus;
    private List<OrderItemResponse> items;
}