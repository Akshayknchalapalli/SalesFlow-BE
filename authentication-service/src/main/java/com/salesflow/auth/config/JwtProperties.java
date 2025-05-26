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
    private String secretKey = "71e2f2a708b6f2ed3afd7b41e374f3caceba1778cd6d80f7ed4d40a1cc067a47ace67c3da81cf007e25f1adca76ce43b2da8024786a5e1acae94cd55bf4cc79b2c3f1a6685b3bcb9a3e2fdaec57291a7d522224a3812e08657bdeb7ce5d5b65b970a628fd68c3cd345d259b14cbc32708f2d385538a38fbf5d062f086878c11e19be001c885666a33628629d849d5dad47eee48dc224b2ebeffcc4f5b0d606fd7f6bce8db14c80e998ef9287d791945ad57238b44b22ba9e5a2c1658ead174da192d062bb32fbf9c7fe5c833692f47e149aabaad1a1ae70a8b500a1bd291143b8e6f873961af13dbd891279ed3b0330031be8d31edb51d04c714107115ae4582";
    
    @Positive(message = "Access token validity must be positive")
    private long accessTokenValidityInMinutes = 30;
    
    @Positive(message = "Refresh token validity must be positive")
    private long refreshTokenValidityInDays = 7;
} 