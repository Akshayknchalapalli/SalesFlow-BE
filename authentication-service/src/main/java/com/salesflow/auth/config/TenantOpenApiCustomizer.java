package com.salesflow.auth.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import com.salesflow.auth.tenant.TenantContext;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Customizes the OpenAPI documentation based on tenant context.
 * This class filters endpoints shown in Swagger UI depending on whether
 * the request is made from a tenant-specific subdomain or from the base domain.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantOpenApiCustomizer implements OpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        // Get current tenant context
        UUID tenantId = TenantContext.getCurrentTenantId();
        String tenantName = TenantContext.getCurrentTenantName();
        
        log.debug("Customizing OpenAPI for tenant context: {} ({})", tenantName, tenantId);
        
        if (tenantId != null) {
            // In tenant context, filter out system admin endpoints
            Map<String, PathItem> filteredPaths = new HashMap<>();
            
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    // Exclude admin endpoints in tenant context
                    if (!path.startsWith("/api/admin/tenants")) {
                        filteredPaths.put(path, pathItem);
                    }
                });
                
                if (!filteredPaths.isEmpty()) {
                    io.swagger.v3.oas.models.Paths paths = new io.swagger.v3.oas.models.Paths();
                    filteredPaths.forEach(paths::addPathItem);
                    openApi.setPaths(paths);
                    log.debug("Filtered API paths for tenant: {}", tenantName);
                }
            }
            
            // Update API title to indicate tenant context
            if (openApi.getInfo() != null) {
                openApi.getInfo().title("Tenant API - " + tenantName);
                openApi.getInfo().description("API documentation for tenant: " + tenantName);
            }
        } else {
            // In system context, we show all endpoints
            log.debug("Showing all API paths for system context");
            
            // Update API title to indicate system context
            if (openApi.getInfo() != null) {
                openApi.getInfo().title("SalesFlow Authentication Service API");
                openApi.getInfo().description("API documentation for the Authentication Service (System Level)");
            }
        }
    }
}