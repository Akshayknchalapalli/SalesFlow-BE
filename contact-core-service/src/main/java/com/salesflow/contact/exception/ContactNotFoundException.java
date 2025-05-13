package com.salesflow.contact.exception;

import java.util.UUID;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException(String message) {
        super(message);
    }

    public ContactNotFoundException(UUID contactId) {
        super("Contact not found with ID: " + contactId);
    }
} 