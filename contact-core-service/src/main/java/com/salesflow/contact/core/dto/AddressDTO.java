package com.salesflow.contact.core.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String type;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean primary;
} 