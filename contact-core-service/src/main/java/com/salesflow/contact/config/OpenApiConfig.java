package com.salesflow.contact.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI contactServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Contact Core Service API")
                        .description("""
                            API documentation for the Contact Management Microservice.
                            
                            ## Features
                            - Create, read, update, and delete contacts
                            - Search and filter contacts
                            - Manage contact stages and tags
                            - Bulk contact operations
                            
                            ## Authentication
                            All endpoints require an X-Owner-Id header for authentication.
                            
                            ## Common Headers
                            - X-Owner-Id: Required for all requests to identify the owner of the contact
                            
                            ## Response Format
                            All responses follow the ContactApiResponse format:
                            ```json
                            {
                                "success": true,
                                "message": "Operation successful message",
                                "data": { ... }
                            }
                            ```
                            """)
                        .version("1.0")
                        .contact(new Contact()
                                .name("SalesFlow Team")
                                .email("support@salesflow.com")
                                .url("https://salesflow.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Default Server URL")
                ))
                .addSecurityItem(new SecurityRequirement().addList("X-Owner-Id"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("X-Owner-Id", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Owner-Id")
                                .description("Owner ID for contact operations")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }
} 