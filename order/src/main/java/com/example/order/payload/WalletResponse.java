package com.example.order.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {
	private Long walletId;
    private Long userId;
    private Double remainingBalance;
    
    public WalletResponse(Double balance) {
    	this.remainingBalance=balance;
    }
}
