package com.salesflow.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @NotBlank(message = "JWT secret key must not be blank")
    private String secretKey = "default-dev-secret-key-for-jwt-authentication-do-not-use-in-production";
    
    @Positive(message = "Access token validity must be positive")
    private long accessTokenValidityInMinutes = 30;
    
    @Positive(message = "Refresh token validity must be positive")
    private long refreshTokenValidityInDays = 7;
} 