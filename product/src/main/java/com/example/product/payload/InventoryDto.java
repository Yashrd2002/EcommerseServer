package com.example.product.payload;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private Long productId;
    private Integer quantityAvailable;
}
