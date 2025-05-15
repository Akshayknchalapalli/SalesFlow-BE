package com.salesflow.auth.config;

import com.salesflow.auth.domain.Role;
import com.salesflow.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestDataInitializer {

    @Bean
    public CommandLineRunner initTestData(@Autowired RoleRepository roleRepository) {
        return args -> {
            // Create default roles
            if (roleRepository.findByName("ROLE_USER").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_USER"));
            }
            
            if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_ADMIN"));
            }
            
            if (roleRepository.findByName("ROLE_TENANT_ADMIN").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_TENANT_ADMIN"));
            }
        };
    }
} 