package com.salesflow.auth.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AuthResponse {
    private String username;
    private String email;
    private String tenantId;
    private Set<String> roles;
} 