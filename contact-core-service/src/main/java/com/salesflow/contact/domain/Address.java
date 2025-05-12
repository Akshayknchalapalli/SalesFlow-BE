package com.salesflow.contact.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import java.time.Instant;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    public enum AddressType {
        HOME, WORK, BILLING, SHIPPING, OTHER
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AddressType type;
    private String street;
    private String city;
    private String state;
    @Column(name = "postal_code")
    private String postalCode;
    private String country;
    @Column(name = "is_primary")
    private boolean primary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
} 