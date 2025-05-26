package com.salesflow.auth.filter;

import com.salesflow.auth.config.TenantProperties;
import com.salesflow.auth.tenant.SubdomainTenantResolver;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Debug filter that logs detailed information about tenant resolution.
 * This is intended for local development only and should be disabled in production.
 */
@Slf4j
@Component
@Order(0) // Run this filter first
@RequiredArgsConstructor
public class TenantDebugFilter extends OncePerRequestFilter {

    private final TenantProperties tenantProperties;
    private final SubdomainTenantResolver tenantResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Only log if we're in local development mode
        if (tenantProperties.isLocalDevelopmentMode()) {
            logRequestDetails(request);
        }

        try {
            UUID tenantId = TenantContext.getCurrentTenantId();
            log.debug("Tenant context before request processing: {}", tenantId);
            
            filterChain.doFilter(request, response);
        } finally {
            UUID tenantId = TenantContext.getCurrentTenantId();
            log.debug("Tenant context after request processing: {}", tenantId);
        }
    }

    private void logRequestDetails(HttpServletRequest request) {
        log.debug("======== Tenant Debug Information ========");
        log.debug("Request URI: {}", request.getRequestURI());
        log.debug("Request URL: {}", request.getRequestURL());
        log.debug("Server Name: {}", request.getServerName());
        log.debug("Server Port: {}", request.getServerPort());
        log.debug("Resolved Tenant ID: {}", tenantResolver.resolveTenantId(request));
        
        // Log all headers
        log.debug("Request Headers:");
        Map<String, String> headers = getRequestHeadersMap(request);
        headers.forEach((name, value) -> log.debug("  {} = {}", name, value));
        
        log.debug("==========================================");
    }

    private Map<String, String> getRequestHeadersMap(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        if (headerNames != null) {
            Collections.list(headerNames).forEach(name -> 
                headers.put(name, request.getHeader(name)));
        }
        
        return headers;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't log static resources
        String path = request.getRequestURI();
        return path.contains("/css/") || 
               path.contains("/js/") || 
               path.contains("/images/") ||
               path.contains("/favicon.ico");
    }
}