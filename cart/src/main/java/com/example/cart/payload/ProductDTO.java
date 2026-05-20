package com.example.cart.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private List<String> images;
    private String description;
    private Double price;
    private Double discount;
    private Double specialPrice;
    private Long categoryId;
    private Long sellerId;
    private int stock;

}