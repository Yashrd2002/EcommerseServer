package com.example.delivery.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponseDto {

    private Long id;
    private Long orderId;
    private Long userId;
    private String courierName;
    private String deliveryStatus;
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private AddressResponse address;  // Include address details
}
