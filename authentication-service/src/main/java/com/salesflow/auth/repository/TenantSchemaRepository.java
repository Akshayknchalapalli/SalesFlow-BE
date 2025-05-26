package com.salesflow.auth.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.salesflow.auth.domain.TenantSchema;

@Repository
public interface TenantSchemaRepository extends JpaRepository<TenantSchema, UUID> {
    List<TenantSchema> findByTenantTenantId(UUID tenantId);
    List<TenantSchema> findByTenantTenantIdAndServiceName(UUID tenantId, String serviceName);
} 