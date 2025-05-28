package com.salesflow.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesflow.auth.tenant.SubdomainTenantResolver;
import com.salesflow.auth.tenant.TenantContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {
    private final TenantProperties tenantProperties;
    private final SubdomainTenantResolver tenantResolver;

    @Bean
    public OpenAPI customOpenAPI() {
        // Get current tenant context
        UUID tenantId = TenantContext.getCurrentTenantId();
        String tenantName = TenantContext.getCurrentTenantName();
        
        // Create base OpenAPI object
        OpenAPI api = new OpenAPI()
                .info(createApiInfo(tenantId, tenantName))
                .servers(getServers())
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", 
                            new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("Enter your JWT token with 'Bearer ' prefix (e.g., 'Bearer your_token')")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
                
        // Register callback to filter paths based on tenant context
        api.addExtension("x-tenant-filter", true);
        
        log.info("Initializing OpenAPI with tenant context: {} ({})", tenantName, tenantId);
        
        return api;
    }
    
    private Info createApiInfo(UUID tenantId, String tenantName) {
        Info info = new Info()
                .version("1.0")
                .contact(new Contact()
                        .name("SalesFlow Team")
                        .email("support@salesflow.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
        
        if (tenantId != null) {
            // Tenant-specific info
            info.title("Tenant API - " + tenantName)
                .description("API documentation for tenant: " + tenantName);
        } else {
            // System-wide info
            info.title("SalesFlow Authentication Service API")
                .description("API documentation for the Authentication Service (System Level)");
        }
        
        return info;
    }
    
    private List<Server> getServers() {
        List<Server> servers = new ArrayList<>();
        
        // Add the current server with the correct tenant subdomain if available
        HttpServletRequest request = null;
        if (RequestContextHolder.getRequestAttributes() != null) {
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }
        
        if (request != null) {
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String scheme = request.getScheme();
            
            // Add the current server
            String serverUrl = scheme + "://" + serverName;
            if (serverPort != 80 && serverPort != 443) {
                serverUrl += ":" + serverPort;
            }
            
            // Get tenant context
            UUID tenantId = TenantContext.getCurrentTenantId();
            String tenantName = TenantContext.getCurrentTenantName();
            
            if (tenantId != null) {
                servers.add(new Server()
                        .url(serverUrl)
                        .description("Current Tenant: " + tenantName));
            } else {
                servers.add(new Server()
                        .url(serverUrl)
                        .description("System Level"));
            }
            
            // If we're in local development mode, add additional options
            if (tenantProperties.isLocalDevelopmentMode()) {
                if (tenantId == null) {
                    // If we're in system context, add tenant options
                    String[] exampleTenants = {"tenant1", "tenant2", "tenant3"};
                    for (String tenant : exampleTenants) {
                        String tenantUrl = scheme + "://" + tenant + ".localhost:" + serverPort;
                        servers.add(new Server()
                                .url(tenantUrl)
                                .description(tenant + " Tenant"));
                    }
                } else {
                    // If we're in tenant context, add system option
                    String systemUrl = scheme + "://localhost:" + serverPort;
                    servers.add(new Server()
                            .url(systemUrl)
                            .description("System Level"));
                }
            }
        } else {
            // Fallback if request is not available
            servers.add(new Server()
                    .url("/")
                    .description("Default Server URL"));
        }
        
        return servers;
    }
    
    /**
     * Filters OpenAPI paths based on tenant context
     * Note: This method would need a SpringDoc customization to be called
     */
    public OpenAPI filterApiForTenant(OpenAPI api) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        
        if (tenantId != null && api.getPaths() != null) {
            // In tenant context, remove admin endpoints
            io.swagger.v3.oas.models.Paths paths = new io.swagger.v3.oas.models.Paths();
            
            api.getPaths().forEach((path, pathItem) -> {
                // Skip admin endpoints in tenant context
                if (!path.startsWith("/api/admin")) {
                    paths.addPathItem(path, pathItem);
                }
            });
            
            api.setPaths(paths);
            log.debug("Filtered API paths for tenant context");
        }
        
        return api;
    }
} 