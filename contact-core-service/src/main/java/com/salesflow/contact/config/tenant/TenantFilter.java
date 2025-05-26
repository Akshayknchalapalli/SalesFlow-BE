package com.salesflow.contact.config.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that extracts the tenant ID from the request and sets it in the TenantContext.
 * This ensures that all database operations in the request's thread use the correct tenant schema.
 * The filter validates the tenant ID against the public.tenants table and handles the new schema
 * structure with service-specific namespaces.
 */
@Slf4j
@Component
@Order(1) // Run this filter first
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private final JdbcTemplate jdbcTemplate;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract tenant ID from header
            String tenantId = request.getHeader(TENANT_HEADER);
            
            // Extract tenant ID from subdomain if not in header
            if (tenantId == null || tenantId.isEmpty()) {
                tenantId = extractTenantFromSubdomain(request);
            }
            
            // Validate tenant ID against public.tenants table
            if (tenantId != null && !tenantId.isEmpty()) {
                try {
                    UUID tenantUuid = UUID.fromString(tenantId);
                    if (isValidTenant(tenantUuid)) {
                        log.debug("Setting tenant context: {}", tenantUuid);
                        TenantContext.setCurrentTenant(tenantUuid);
                    } else {
                        log.warn("Invalid tenant ID: {}", tenantUuid);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid tenant ID");
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID format for tenant ID: {}", tenantId);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid tenant ID format");
                    return;
                }
            } else {
                log.warn("No tenant ID found in request");
            }
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the tenant context to prevent leaks
            TenantContext.clear();
            log.debug("Cleared tenant context");
        }
    }
    
    /**
     * Validates if the tenant ID exists and is active in the public.tenants table
     */
    private boolean isValidTenant(UUID tenantId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.tenants WHERE tenant_id = ? AND active = true",
                Integer.class,
                tenantId
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error validating tenant ID: {}", tenantId, e);
            return false;
        }
    }
    
    /**
     * Extracts tenant ID from the subdomain of the request.
     * For example, from "tenant1.salesflow.com" it would extract "tenant1".
     */
    private String extractTenantFromSubdomain(HttpServletRequest request) {
        String serverName = request.getServerName();
        if (serverName != null && serverName.contains(".")) {
            // Extract subdomain
            int firstDotIndex = serverName.indexOf('.');
            if (firstDotIndex > 0) {
                String subdomain = serverName.substring(0, firstDotIndex);
                log.debug("Extracted tenant from subdomain: {}", subdomain);
                return subdomain;
            }
        }
        return null;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip for static resources and specific endpoints
        return path.contains("/actuator") || 
               path.contains("/swagger-ui") || 
               path.contains("/api-docs") ||
               path.contains("/health");
    }
}