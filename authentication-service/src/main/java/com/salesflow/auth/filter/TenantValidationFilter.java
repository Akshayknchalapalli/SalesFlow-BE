package com.salesflow.auth.filter;

import com.salesflow.auth.tenant.SubdomainTenantResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantValidationFilter extends OncePerRequestFilter {
    private final SubdomainTenantResolver tenantResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Try to resolve tenant from subdomain first, then fallback to header
        UUID tenantId = tenantResolver.resolveTenantId(request);
        
        // Skip tenant validation for public endpoints
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate tenant ID if present
        if (tenantId != null) {
            // Add the tenant ID as a request attribute so it can be accessed by other components
            request.setAttribute("CURRENT_TENANT_ID", tenantId);
        } else {
            // For protected endpoints that require a tenant ID
            if (requiresTenantId(request.getRequestURI())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Tenant ID is required for this endpoint. Please access via tenant subdomain or provide X-Tenant-ID header.");
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
               uri.startsWith("/swagger-ui") ||
               uri.startsWith("/v3/api-docs") ||
               uri.startsWith("/swagger-ui.html") ||
               uri.startsWith("/actuator");
    }
    
    private boolean requiresTenantId(String uri) {
        return uri.startsWith("/api/auth/tenant/") ||
               uri.startsWith("/api/contacts/") ||
               uri.startsWith("/api/activities/");
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