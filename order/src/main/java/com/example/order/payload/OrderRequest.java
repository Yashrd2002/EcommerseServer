package com.example.order.payload;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private String email;
    private Long addressId;
}