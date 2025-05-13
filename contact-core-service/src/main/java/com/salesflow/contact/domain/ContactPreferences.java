package com.salesflow.contact.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import com.salesflow.contact.domain.Contact.ContactMethod;
import com.salesflow.contact.domain.Contact.ContactTime;

@Embeddable
@Getter
@Setter
public class ContactPreferences {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method")
    private ContactMethod preferredContactMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_time")
    private ContactTime preferredContactTime;
    
    @Column(name = "do_not_contact")
    private boolean doNotContact;
    
    @Column(name = "marketing_opt_in")
    private boolean marketingOptIn;
    
    @Column(name = "communication_language", length = 10)
    private String communicationLanguage;
} 