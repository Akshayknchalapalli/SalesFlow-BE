package com.salesflow.activity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI activityServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Contact Activity Service API")
                        .description("API for managing contact activities in the SalesFlow CRM")
                        .version("1.0")
                        .contact(new Contact()
                                .name("SalesFlow Team")
                                .email("support@salesflow.com")
                                .url("https://salesflow.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
} 