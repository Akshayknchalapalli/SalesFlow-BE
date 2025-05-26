package com.salesflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUserResponse {
    private UUID id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;
}