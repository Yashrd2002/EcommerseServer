package com.example.product.payload;

import java.util.List;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String productName;

    private List<String> images;

    @NotBlank(message = "Product description is required")
    @Size(min = 6, max = 500, message = "Product description must be between 6 and 500 characters")
    private String description;


    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private double price;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private double discount;

    @DecimalMin(value = "0.0", message = "Special price cannot be negative")
    private double specialPrice;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;

    // 🆕 Added: Average rating field
    private Double averageRating;
    
    private Long sellerId;
    
    private int stock;
}
