package com.salesflow.auth.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesflow.auth.dto.ApiResponseWrapper;
import com.salesflow.auth.tenant.TenantContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/debug/tenant")
@RequiredArgsConstructor
@Tag(name = "Tenant Debug", description = "Endpoints for debugging tenant resolution")
public class TenantDebugController {

    @Operation(summary = "Get current tenant context", description = "Returns information about the current tenant context")
    @GetMapping("/context")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> getTenantContext(HttpServletRequest request) {
        Map<String, Object> context = new HashMap<>();
        
        // Get tenant context
        UUID tenantId = TenantContext.getCurrentTenantId();
        String tenantName = TenantContext.getCurrentTenantName();
        
        // Basic context info
        context.put("tenant_id", tenantId);
        context.put("tenant_name", tenantName);
        context.put("has_tenant_context", tenantId != null);
        
        // Request info
        context.put("server_name", request.getServerName());
        context.put("server_port", request.getServerPort());
        context.put("request_uri", request.getRequestURI());
        context.put("request_url", request.getRequestURL().toString());
        context.put("remote_addr", request.getRemoteAddr());
        
        // Headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name -> 
            headers.put(name, request.getHeader(name))
        );
        context.put("headers", headers);
        
        log.debug("Tenant context debug request: {}", context);
        
        return ResponseEntity.ok(new ApiResponseWrapper<>(
                true, "Tenant context retrieved", context));
    }
}