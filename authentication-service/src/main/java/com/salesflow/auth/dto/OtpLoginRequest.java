package com.salesflow.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpLoginRequest {
    @NotBlank(message = "Identifier is required")
    private String identifier; // Email or username
    
    @NotBlank(message = "OTP is required")
    private String otp;
}