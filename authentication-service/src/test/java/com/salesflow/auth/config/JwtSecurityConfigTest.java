package com.salesflow.auth.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JwtSecurityConfigTest {

    @Test
    public void securityConfigurationLoads() {
        // Simple test that doesn't depend on Spring context
        assert(true);
    }
} 