package com.salesflow.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.salesflow.auth.domain.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByTenantId(UUID tenantId);
    boolean existsByTenantId(UUID tenantId);
} 