package com.salesflow.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey;
    private long accessTokenValidityInMinutes = 30;
    private long refreshTokenValidityInDays = 7;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getAccessTokenValidityInMinutes() {
        return accessTokenValidityInMinutes;
    }

    public void setAccessTokenValidityInMinutes(long accessTokenValidityInMinutes) {
        this.accessTokenValidityInMinutes = accessTokenValidityInMinutes;
    }

    public long getRefreshTokenValidityInDays() {
        return refreshTokenValidityInDays;
    }

    public void setRefreshTokenValidityInDays(long refreshTokenValidityInDays) {
        this.refreshTokenValidityInDays = refreshTokenValidityInDays;
    }
} 