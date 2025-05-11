package com.salesflow.contact.dto;

import lombok.Data;

@Data
public class ContactPreferencesDTO {
    private String preferredContactMethod;
    private String preferredContactTime;
    private boolean doNotContact;
    private boolean marketingOptIn;
    private String communicationLanguage;
} 