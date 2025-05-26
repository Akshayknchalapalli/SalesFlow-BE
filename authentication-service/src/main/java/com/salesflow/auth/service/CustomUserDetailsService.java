package com.salesflow.auth.service;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.UserRepository;
import com.salesflow.auth.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        try {
            // Get the current tenant ID from the context
            UUID currentTenantId = TenantContext.getCurrentTenantId();
            User user;
            
            if (currentTenantId != null) {
                log.debug("Looking up user in tenant: {}", currentTenantId);
                // If we have a tenant context, look up the user in that tenant
                user = userRepository.findByUsernameAndTenantId(username, currentTenantId)
                        .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with username: " + username + " in tenant: " + currentTenantId));
            } else {
                // Fall back to global lookup if no tenant context is available
                log.debug("No tenant context, performing global user lookup");
                user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
            }
            
            log.debug("User found: {}", user.getUsername());
            return new CustomUserDetails(user);
        } catch (UsernameNotFoundException e) {
            log.warn("User not found: {}", username);
            throw e;
        } catch (Exception e) {
            log.error("Error loading user by username: {}", username, e);
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }
} 