package com.salesflow.auth.controller;

import com.salesflow.auth.dto.ApiResponseWrapper;
import com.salesflow.auth.dto.TenantCreationRequest;
import com.salesflow.auth.dto.TenantInfoResponse;
import com.salesflow.auth.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Administration", description = "APIs for tenant management (system admin only)")
public class TenantAdminController {

    private final TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all tenants",
        description = "Returns information about all tenants. Requires ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<List<TenantInfoResponse>>> getAllTenants() {
        List<TenantInfoResponse> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, "Tenants retrieved successfully", tenants));
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get tenant by ID",
        description = "Returns information about a specific tenant. Requires ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<TenantInfoResponse>> getTenantById(@PathVariable UUID tenantId) {
        TenantInfoResponse tenant = tenantService.getTenantInfo(tenantId);
        if (tenant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, "Tenant retrieved successfully", tenant));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new tenant",
        description = "Creates a new tenant with initial admin user. Requires ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<TenantInfoResponse>> createTenant(
            @Valid @RequestBody TenantCreationRequest request) {
        try {
            TenantInfoResponse tenant = tenantService.createTenant(request);
            return ResponseEntity.ok(new ApiResponseWrapper<>(
                    true, "Tenant created successfully", tenant));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponseWrapper<>(
                    false, e.getMessage(), null));
        }
    }

    @PutMapping("/{tenantId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Deactivate tenant",
        description = "Deactivates a tenant. Requires ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<Void>> deactivateTenant(@PathVariable UUID tenantId) {
        tenantService.deactivateTenant(tenantId);
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, "Tenant deactivated successfully", null));
    }

    @PutMapping("/{tenantId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Reactivate tenant",
        description = "Reactivates a deactivated tenant. Requires ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<Void>> reactivateTenant(@PathVariable UUID tenantId) {
        tenantService.reactivateTenant(tenantId);
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, "Tenant reactivated successfully", null));
    }

    @GetMapping("/{tenantId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get tenant statistics",
        description = "Returns statistics for a specific tenant. Requires ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> getTenantStatistics(@PathVariable UUID tenantId) {
        Map<String, Object> statistics = tenantService.getTenantStatistics(tenantId);
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, "Tenant statistics retrieved successfully", statistics));
    }

    @GetMapping("/check-availability")
    @Operation(
        summary = "Check tenant ID availability",
        description = "Checks if a tenant ID is available for use."
    )
    public ResponseEntity<ApiResponseWrapper<Boolean>> checkTenantIdAvailability(@RequestParam UUID tenantId) {
        boolean available = tenantService.isTenantIdAvailable(tenantId);
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, "Tenant ID availability checked", available));
    }
}