package com.salesflow.auth.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.salesflow.auth.domain.TenantAuditLog;

@Repository
public interface TenantAuditLogRepository extends JpaRepository<TenantAuditLog, UUID> {
    List<TenantAuditLog> findByTenantTenantIdOrderByCreatedAtDesc(UUID tenantId);
} 