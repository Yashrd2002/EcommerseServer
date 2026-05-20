package com.example.order.payload;



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

    // Product details
    private String productName;
    private List<String> images;
    private Double price;
    private Double specialPrice;
    
    private Long sellerId;
    
    public CartItemResponse(Long cartItemId,String productName,Integer quantity,Double price,Double specialPrice,Long sellerId) {
    	this.cartItemId=cartItemId;
    	this.productName=productName;
    	this.quantity=quantity;
    	this.price=price;
    	this.specialPrice=specialPrice;
    	this.sellerId=sellerId;
    }
}