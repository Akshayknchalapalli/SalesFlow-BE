package com.salesflow.auth.filter;

import com.salesflow.auth.tenant.SubdomainTenantResolver;
import com.salesflow.auth.tenant.SubdomainTenantResolver.TenantInfo;
import com.salesflow.auth.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter that sets the tenant context based on the request domain or header.
 * This filter runs before the security filters to ensure the tenant context
 * is available for tenant-specific security checks.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final SubdomainTenantResolver tenantResolver;
    
    // Paths that don't require tenant resolution
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/public",
        "/actuator",
        "/v3/api-docs",
        "/swagger-ui"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Always clear the tenant context before processing a new request
        TenantContext.clear();
        
        try {
            String path = request.getRequestURI();
            
            // Skip tenant resolution for public paths
            if (isPublicPath(path)) {
                log.debug("Skipping tenant resolution for public path: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // Resolve tenant from subdomain or header
            TenantInfo tenantInfo = tenantResolver.resolveTenant(request);
            
            if (tenantInfo != null) {
                // Set the tenant context
                TenantContext.setCurrentTenant(tenantInfo.getTenantId(), tenantInfo.getTenantName());
                log.debug("Set tenant context: {} ({}) for request: {}",
                    tenantInfo.getTenantName(), tenantInfo.getTenantId(), request.getRequestURI());
                
                // Add tenant info to request attributes for potential use in controllers
                request.setAttribute("tenantId", tenantInfo.getTenantId());
                request.setAttribute("tenantName", tenantInfo.getTenantName());
            } else {
                log.debug("No tenant resolved for request: {}", request.getRequestURI());
            }
            
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the tenant context after the request is processed
            TenantContext.clear();
            log.trace("Cleared tenant context after request processing");
        }
    }
    
    /**
     * Determines if a path is public (doesn't require tenant context)
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}