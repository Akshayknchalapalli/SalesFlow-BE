package com.salesflow.contact.event;

import com.salesflow.contact.domain.Contact;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ContactEvent {
    private String eventType;
    private Contact contact;
    private Instant timestamp;
    private String userId;

    public ContactEvent(String eventType, Contact contact, String userId) {
        this.eventType = eventType;
        this.contact = contact;
        this.timestamp = Instant.now();
        this.userId = userId;
    }

    public enum EventType {
        CONTACT_CREATED,
        CONTACT_UPDATED,
        CONTACT_DELETED,
        CONTACT_STAGE_CHANGED
    }
} 