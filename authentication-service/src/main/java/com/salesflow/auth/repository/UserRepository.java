package com.salesflow.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.salesflow.auth.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameAndTenantId(String username, UUID tenantId);
    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);
    List<User> findAllByTenantId(UUID tenantId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndTenantId(String username, UUID tenantId);
    boolean existsByEmailAndTenantId(String email, UUID tenantId);
    
    long countByTenantId(UUID tenantId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = ?1 AND u.enabled = true")
    long countActiveUsersByTenantId(UUID tenantId);
}