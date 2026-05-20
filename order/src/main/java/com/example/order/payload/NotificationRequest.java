package com.example.order.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Subject cannot be empty")
    private String subject;

    @NotBlank(message = "Message cannot be empty")
    private String message;
}
