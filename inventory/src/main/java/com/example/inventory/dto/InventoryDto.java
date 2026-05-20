package com.example.inventory.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private Long productId;
    private Integer quantityAvailable;
}
