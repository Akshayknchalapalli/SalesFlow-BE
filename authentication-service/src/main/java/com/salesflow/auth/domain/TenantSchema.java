package com.salesflow.auth.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tenant_schemas", schema = "public")
@EqualsAndHashCode(callSuper = true)
public class TenantSchema extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "schema_name", nullable = false)
    private String schemaName;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "migration_version")
    private String migrationVersion;

    @Column(name = "migration_status")
    private String migrationStatus;

    @Column(name = "last_validation_at")
    private OffsetDateTime lastValidationAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;
} 