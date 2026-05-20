package com.example.delivery.dto;

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
    private AddressResponse address; // full address details
    private List<OrderItemDto> items;
}
