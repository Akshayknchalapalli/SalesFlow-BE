package com.salesflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    private String requestedRole;
} 