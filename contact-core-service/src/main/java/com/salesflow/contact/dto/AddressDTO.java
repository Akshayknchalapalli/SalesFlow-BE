package com.salesflow.contact.dto;

import com.salesflow.contact.domain.Address.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private AddressType type;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean primary;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
} 