package com.example.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpVerificationRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String otp;
}