package com.salesflow.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class TenantValidationFilter extends OncePerRequestFilter {

    private static final Pattern TENANT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
    private static final int MIN_TENANT_ID_LENGTH = 3;
    private static final int MAX_TENANT_ID_LENGTH = 50;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String tenantId = request.getHeader("X-Tenant-ID");
        
        // Skip tenant validation for public endpoints
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate tenant ID if present
        if (tenantId != null) {
            if (!isValidTenantId(tenantId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid tenant ID format. Must be 3-50 characters and contain only letters, numbers, hyphens, and underscores.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String uri) {
        return uri.startsWith("/api/auth/register") ||
               uri.startsWith("/api/auth/login") ||
               uri.startsWith("/api/auth/refresh") ||
               uri.startsWith("/api-docs") ||
               uri.startsWith("/swagger-ui");
    }

    private boolean isValidTenantId(String tenantId) {
        return tenantId.length() >= MIN_TENANT_ID_LENGTH &&
               tenantId.length() <= MAX_TENANT_ID_LENGTH &&
               TENANT_ID_PATTERN.matcher(tenantId).matches();
    }
} 