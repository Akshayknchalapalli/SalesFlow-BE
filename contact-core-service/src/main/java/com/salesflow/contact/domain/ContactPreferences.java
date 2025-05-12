package com.salesflow.contact.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ContactPreferences {
    
    @Column(name = "preferred_contact_method")
    private String preferredContactMethod;
    
    @Column(name = "preferred_contact_time")
    private String preferredContactTime;
    
    @Column(name = "do_not_contact")
    private boolean doNotContact;
    
    @Column(name = "marketing_opt_in")
    private boolean marketingOptIn;
    
    @Column(name = "communication_language")
    private String communicationLanguage;
} 