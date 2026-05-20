package com.example.delivery.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequestDto {
    private Long orderId;
    private Long userId;
    private Long addressId;
    private String courierName;
    private LocalDate estimatedDeliveryDate;
}
