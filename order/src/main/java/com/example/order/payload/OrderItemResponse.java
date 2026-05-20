package com.example.order.payload;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private double price;
    private double discount;
}