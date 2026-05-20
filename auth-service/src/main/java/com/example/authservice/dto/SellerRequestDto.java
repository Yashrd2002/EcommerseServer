package com.example.authservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class SellerRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business type is required")
    private String businessType;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;
}
