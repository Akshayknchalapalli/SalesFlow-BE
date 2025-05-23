package com.salesflow.auth.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.salesflow.auth.domain.Role;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.RoleRepository;
import com.salesflow.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // Create roles if they don't exist
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_ADMIN");
                        return roleRepository.save(role);
                    });

            Role tenantAdminRole = roleRepository.findByName("ROLE_TENANT_ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_TENANT_ADMIN");
                        return roleRepository.save(role);
                    });

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_USER");
                        return roleRepository.save(role);
                    });

            // Create system admin user if it doesn't exist
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setEmail("admin@system.com");
                adminUser.setTenantId("system");
                adminUser.setEnabled(true);
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                adminUser.setRoles(adminRoles);
                userRepository.save(adminUser);
            }

            // Create tenant admin user if it doesn't exist
            if (!userRepository.existsByUsername("tenant-admin")) {
                User tenantAdmin = new User();
                tenantAdmin.setUsername("tenant-admin");
                tenantAdmin.setPassword(passwordEncoder.encode("tenant123"));
                tenantAdmin.setEmail("admin@tenant.com");
                tenantAdmin.setTenantId("tenant1");
                tenantAdmin.setEnabled(true);
                Set<Role> tenantAdminRoles = new HashSet<>();
                tenantAdminRoles.add(tenantAdminRole);
                tenantAdmin.setRoles(tenantAdminRoles);
                userRepository.save(tenantAdmin);
            }

            // Create regular user if it doesn't exist
            if (!userRepository.existsByUsername("user")) {
                User regularUser = new User();
                regularUser.setUsername("user");
                regularUser.setPassword(passwordEncoder.encode("user123"));
                regularUser.setEmail("user@tenant.com");
                regularUser.setTenantId("tenant1");
                regularUser.setEnabled(true);
                Set<Role> userRoles = new HashSet<>();
                userRoles.add(userRole);
                regularUser.setRoles(userRoles);
                userRepository.save(regularUser);
            }
        };
    }
} 