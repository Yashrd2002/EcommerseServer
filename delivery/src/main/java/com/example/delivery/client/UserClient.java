package com.example.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.delivery.dto.AddressResponse;
import com.example.delivery.dto.UserResponse;



@FeignClient(name = "auth-service", url = "http://localhost:8080/auth",configuration = FeignClientConfig.class)

public interface UserClient {
	@GetMapping("/address/{id}")
    AddressResponse getAddressesById(@PathVariable("id") Long id);
	
	@GetMapping("/getUser/{userId}")
    ResponseEntity<UserResponse> getUser(@PathVariable("userId") Long userId);
}
