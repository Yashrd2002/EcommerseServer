package com.example.order.payload;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDate orderDate;
    private String orderStatus;
    private double totalAmount;
    private String paymentMethod;    // optional: WALLET / CARD
    private AddressResponse address;
    private List<OrderItemDto> items;
    private String paymentStatus;
    private String message;
}
