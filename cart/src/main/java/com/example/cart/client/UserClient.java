package com.example.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.cart.payload.UserResponse;


@FeignClient(name = "auth-service", url = "http://localhost:8081/auth", configuration = FeignClientConfig.class)

public interface UserClient {
    @GetMapping("/getUser")
    ResponseEntity<UserResponse> getUser(@RequestHeader("X-User-Name") String username);
}
