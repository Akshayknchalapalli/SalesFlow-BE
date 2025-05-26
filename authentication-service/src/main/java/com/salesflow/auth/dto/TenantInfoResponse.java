package com.salesflow.auth.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfoResponse {
    private UUID tenantId;
    private String name;
    private String domain;
    private boolean active;
    private String plan;
    private String createdAt;
}