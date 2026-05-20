package com.example.order.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.order.payload.AddressResponse;
import com.example.order.payload.UserResponse;



@FeignClient(name = "auth-service", url = "http://localhost:8080/auth",configuration = FeignClientConfig.class)

public interface UserClient {
	@GetMapping("/getUser/{userId}")
    ResponseEntity<UserResponse> getUser(@PathVariable("userId") Long userId);
    
    @GetMapping("/address/list")
    public ResponseEntity<List<AddressResponse>> getAddresses(@RequestHeader("X-User-Id") Long userId);
    
    @GetMapping("/address/{id}")
    ResponseEntity<AddressResponse> getAddressesById(@PathVariable("id") Long id);
}
