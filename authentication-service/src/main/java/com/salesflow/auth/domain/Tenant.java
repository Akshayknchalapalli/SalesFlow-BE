package com.salesflow.auth.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Tenant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", unique = true, nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Column
    private String domain;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "tenant_plan")
    private String tenantPlan;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_storage_mb")
    private Integer maxStorageMb;

    @Column(name = "owner_email")
    private String ownerEmail;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column
    private String notes;
} 