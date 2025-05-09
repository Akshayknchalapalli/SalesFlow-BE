package com.salesflow.contact.core.dto;

import com.salesflow.contact.core.domain.Contact;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class ContactDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String companyName;
    private String jobTitle;
    private Contact.ContactStage stage;
    private String ownerId;
    private ContactPreferencesDTO preferences;
    private Set<AddressDTO> addresses;
    private Set<SocialProfileDTO> socialProfiles;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
} 