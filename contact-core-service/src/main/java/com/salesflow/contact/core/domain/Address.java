package com.salesflow.contact.core.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;

@Embeddable
@Getter
@Setter
public class Address {
    
    @Column(name = "address_type")
    private String type; // e.g., HOME, WORK, BILLING
    
    private String street;
    
    private String city;
    
    private String state;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    private String country;
    
    @Column(name = "is_primary")
    private boolean primary;
} 