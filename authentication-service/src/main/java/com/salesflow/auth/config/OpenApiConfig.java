package com.salesflow.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesflow.auth.tenant.SubdomainTenantResolver;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {
    private final TenantProperties tenantProperties;
    private final SubdomainTenantResolver tenantResolver;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authentication Service API")
                        .description("API documentation for the Authentication Service")
                        .version("1.0")
                        .contact(new Contact()
                                .name("SalesFlow Team")
                                .email("support@salesflow.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
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
            
            servers.add(new Server()
                    .url(serverUrl)
                    .description("Current Server"));
            
            // If we're in local development mode, add tenant-specific servers
            if (tenantProperties.isLocalDevelopmentMode()) {
                // Add some example tenant servers for testing
                String[] exampleTenants = {"tenant1", "tenant2", "tenant3"};
                for (String tenant : exampleTenants) {
                    String tenantUrl = scheme + "://" + tenant + ".localhost:" + serverPort;
                    servers.add(new Server()
                            .url(tenantUrl)
                            .description(tenant + " Tenant"));
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
} 