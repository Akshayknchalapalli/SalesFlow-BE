package com.salesflow.auth.filter;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.salesflow.auth.tenant.TenantContext;
import com.salesflow.auth.service.TenantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filter that sets and clears the current tenant ID in the TenantContext.
 * This filter should be registered early in the filter chain.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {
    private final TenantService tenantService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String tenantIdHeader = request.getHeader("X-Tenant-ID");
            if (tenantIdHeader != null && !tenantIdHeader.isEmpty()) {
                try {
                    UUID tenantId = UUID.fromString(tenantIdHeader);
                    tenantService.setCurrentTenant(tenantId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid tenant ID format in header: {}", tenantIdHeader);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            tenantService.clearCurrentTenant();
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || 
               path.startsWith("/api-docs") || 
               path.startsWith("/v3/api-docs") ||
               path.equals("/swagger-ui.html");
    }
}