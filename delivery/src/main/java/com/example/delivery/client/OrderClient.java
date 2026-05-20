package com.example.delivery.client;


import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.delivery.dto.OrderDto;



@FeignClient(name = "ORDER-SERVICE",configuration = FeignClientConfig.class)
public interface OrderClient {

    @GetMapping("/orders/seller/getOrder/{orderId}")
    OrderDto getOrderByIdForSeller(
            @RequestHeader("X-User-Id") Long sellerId,
            @PathVariable("orderId") Long orderId
    );
}
