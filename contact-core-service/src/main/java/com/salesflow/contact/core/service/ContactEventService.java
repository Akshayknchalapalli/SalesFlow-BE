package com.salesflow.contact.core.service;

import com.salesflow.contact.core.domain.Contact;
import com.salesflow.contact.core.event.ContactEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactEventService {
    private final KafkaTemplate<String, ContactEvent> kafkaTemplate;
    private static final String TOPIC = "contact-events";

    public void publishContactCreated(Contact contact, String userId) {
        publishEvent(ContactEvent.EventType.CONTACT_CREATED.name(), contact, userId);
    }

    public void publishContactUpdated(Contact contact, String userId) {
        publishEvent(ContactEvent.EventType.CONTACT_UPDATED.name(), contact, userId);
    }

    public void publishContactDeleted(Contact contact, String userId) {
        publishEvent(ContactEvent.EventType.CONTACT_DELETED.name(), contact, userId);
    }

    public void publishStageChanged(Contact contact, String userId) {
        publishEvent(ContactEvent.EventType.CONTACT_STAGE_CHANGED.name(), contact, userId);
    }

    private void publishEvent(String eventType, Contact contact, String userId) {
        ContactEvent event = new ContactEvent(eventType, contact, userId);
        kafkaTemplate.send(TOPIC, contact.getId().toString(), event);
    }
} 