package com.salesflow.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {TestSecurityConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
public class JwtSecurityConfigTest {

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void securityConfigurationLoads() {
        // Test that the security configuration can load successfully
    }
} 