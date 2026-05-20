package com.example.cart.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long cartId;
    private Long userId;
    private int totalItems;
    private int totalQuantity;
    private List<CartItemResponse> items;

}
