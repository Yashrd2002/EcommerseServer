package com.example.cart.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {

    private Long cartItemId;
    private Long productId;
    private Integer quantity;
    private String productName;
    private List<String> images;
    private Double price;
    private Double specialPrice;
    private Long sellerId;
    
    private Integer stockAvailable;

}