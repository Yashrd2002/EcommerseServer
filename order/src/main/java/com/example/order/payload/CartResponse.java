package com.example.order.payload;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private Long userId;
    private Integer totalItems;
    private Integer totalQuantity;
    private Long sellerId;
    private List<CartItemResponse> items;
    
    public CartResponse(List<CartItemResponse> items) {
    	this.items=items;
    }
}
