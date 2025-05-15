package com.salesflow.auth.config;

import com.salesflow.auth.domain.Role;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.RoleRepository;
import com.salesflow.auth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initData() {
        if (roleRepository.count() == 0) {
            createRoles();
        }
        
        if (userRepository.count() == 0) {
            createUsers();
        }
    }
    
    private void createRoles() {
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        
        Role tenantAdminRole = new Role();
        tenantAdminRole.setName("ROLE_TENANT_ADMIN");
        
        roleRepository.saveAll(List.of(userRole, adminRole, tenantAdminRole));
    }
    
    private void createUsers() {
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role tenantAdminRole = roleRepository.findByName("ROLE_TENANT_ADMIN").orElseThrow();
        
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setTenantId("master");
        adminUser.setEnabled(true);
        adminUser.setAuthorities(Set.of(adminRole, userRole));
        
        User tenantAdmin = new User();
        tenantAdmin.setUsername("tenant1admin");
        tenantAdmin.setEmail("tenant1admin@example.com");
        tenantAdmin.setPassword(passwordEncoder.encode("password"));
        tenantAdmin.setTenantId("tenant1");
        tenantAdmin.setEnabled(true);
        tenantAdmin.setAuthorities(Set.of(tenantAdminRole, userRole));
        
        User regularUser = new User();
        regularUser.setUsername("user1");
        regularUser.setEmail("user1@example.com");
        regularUser.setPassword(passwordEncoder.encode("password"));
        regularUser.setTenantId("tenant1");
        regularUser.setEnabled(true);
        regularUser.setAuthorities(Set.of(userRole));
        
        userRepository.saveAll(List.of(adminUser, tenantAdmin, regularUser));
    }
} 