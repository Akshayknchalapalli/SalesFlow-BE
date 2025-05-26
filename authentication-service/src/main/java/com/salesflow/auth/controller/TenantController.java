package com.salesflow.auth.controller;

import com.salesflow.auth.domain.User;
import com.salesflow.auth.dto.ApiResponseWrapper;
import com.salesflow.auth.dto.TenantInfoResponse;
import com.salesflow.auth.dto.TenantUserResponse;
import com.salesflow.auth.repository.UserRepository;
import com.salesflow.auth.service.CustomUserDetails;
import com.salesflow.auth.service.TenantService;
import com.salesflow.auth.tenant.SubdomainTenantResolver;
import com.salesflow.auth.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "APIs for tenant-specific operations")
public class TenantController {

    private final UserRepository userRepository;
    private final SubdomainTenantResolver tenantResolver;
    private final TenantService tenantService;

    @GetMapping("/current")
    public ResponseEntity<UUID> getCurrentTenant() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        return ResponseEntity.ok(tenantId);
    }

    @GetMapping("/info")
    @Operation(
        summary = "Get tenant information",
        description = "Returns information about the current tenant",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<TenantInfoResponse>> getTenantInfo(
            HttpServletRequest request,
            Authentication authentication) {
        
        UUID tenantId = TenantContext.getCurrentTenantId();
        String tenantName = TenantContext.getCurrentTenantName();
        if (tenantId == null && authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            tenantId = userDetails.getTenantId();
        }
        
        if (tenantId == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, "No tenant context available", null));
        }
        
        TenantInfoResponse response = TenantInfoResponse.builder()
                .tenantId(tenantId)
                .name(tenantName != null ? tenantName : tenantService.getTenantNameById(tenantId))
                .domain(tenantResolver.getTenantDomain(tenantId))
                .active(true)
                .build();
        
        return ResponseEntity.ok(new ApiResponseWrapper<>(true, "Tenant information retrieved successfully", response));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(
        summary = "Get tenant users",
        description = "Returns all users in the current tenant. Requires TENANT_ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<List<TenantUserResponse>>> getTenantUsers() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, "No tenant context available", null));
        }
        
        List<User> users = userRepository.findAllByTenantId(tenantId);
        List<TenantUserResponse> userResponses = users.stream()
                .map(user -> TenantUserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .enabled(user.isEnabled())
                        .roles(user.getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, 
                "Users retrieved successfully for tenant: " + tenantId, 
                userResponses
        ));
    }

    @PostMapping("/switch/{tenantId}")
    @Operation(
        summary = "Switch tenant",
        description = "Switch to a different tenant if the user has access to it",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponseWrapper<TenantInfoResponse>> switchTenant(
            @PathVariable UUID tenantId,
            Authentication authentication) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, "Authentication required", null));
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // Check if user has access to the requested tenant
        // This is a simplified check - in a real system you'd have a user-tenant relationship table
        if (!tenantId.toString().equals(userDetails.getTenantId()) && !userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, "You do not have access to the requested tenant", null));
        }
        
        // Switch tenant context
        String tenantName = tenantService.getTenantNameById(tenantId);
        TenantContext.setCurrentTenant(tenantId, tenantName);
        
        TenantInfoResponse response = TenantInfoResponse.builder()
                .tenantId(tenantId)
                .domain(tenantResolver.getTenantDomain(tenantId))
                .active(true)
                .build();
        
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, 
                "Switched to tenant: " + tenantId, 
                response
        ));
    }
}