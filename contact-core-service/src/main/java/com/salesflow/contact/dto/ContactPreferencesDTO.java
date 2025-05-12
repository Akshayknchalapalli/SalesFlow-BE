package com.salesflow.contact.dto;

import com.salesflow.contact.domain.Contact.ContactMethod;
import com.salesflow.contact.domain.Contact.ContactTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactPreferencesDTO {
    private ContactMethod preferredContactMethod;
    private ContactTime preferredContactTime;
    private boolean doNotContact;
    private boolean marketingOptIn;
    private String communicationLanguage;
} 