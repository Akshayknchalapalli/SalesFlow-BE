package com.salesflow.auth.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.salesflow.auth.domain.TenantFeature;

@Repository
public interface TenantFeatureRepository extends JpaRepository<TenantFeature, UUID> {
    List<TenantFeature> findByTenantTenantId(UUID tenantId);
    boolean existsByTenantTenantIdAndFeatureKey(UUID tenantId, String featureKey);
} 