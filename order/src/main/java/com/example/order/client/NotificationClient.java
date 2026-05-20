package com.example.order.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.order.payload.NotificationRequest;

@FeignClient(name = "NOTIFICATION-SERVICE", configuration = FeignClientConfig.class)
public interface NotificationClient {

    @PostMapping("/notifications/send") 
    void sendNotification(@RequestBody NotificationRequest request);
}
