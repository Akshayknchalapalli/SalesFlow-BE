package com.salesflow.auth.config;

import com.salesflow.auth.domain.Role;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.RoleRepository;
import com.salesflow.auth.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE) // Run after migrations
    CommandLineRunner initData() {
        return args -> {
            // Create roles if they don't exist
            Role adminRole = roleRepository
                .findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_ADMIN");
                    return roleRepository.save(role);
                });

            Role tenantAdminRole = roleRepository
                .findByName("ROLE_TENANT_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_TENANT_ADMIN");
                    return roleRepository.save(role);
                });

            Role userRole = roleRepository
                .findByName("ROLE_USER")
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
                adminUser.setTenantId(
                    UUID.fromString("00000000-0000-0000-0000-000000000001")
                );
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
                tenantAdmin.setTenantId(
                    UUID.fromString("00000000-0000-0000-0000-000000000001")
                ); // Default tenant ID
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
                regularUser.setTenantId(
                    UUID.fromString("00000000-0000-0000-0000-000000000001")
                ); // Default tenant ID
                regularUser.setEnabled(true);
                Set<Role> userRoles = new HashSet<>();
                userRoles.add(userRole);
                regularUser.setRoles(userRoles);
                userRepository.save(regularUser);
            }
        };
    }
}
