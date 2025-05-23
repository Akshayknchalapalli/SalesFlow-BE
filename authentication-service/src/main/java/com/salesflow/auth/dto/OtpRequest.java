package com.salesflow.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {
    @NotBlank(message = "Identifier is required")
    private String identifier; // Email or username
    
    @NotBlank(message = "Channel is required")
    private String channel; // "email", "sms", or "whatsapp"
    
    // Phone number field for SMS/WhatsApp
    private String phoneNumber;
}