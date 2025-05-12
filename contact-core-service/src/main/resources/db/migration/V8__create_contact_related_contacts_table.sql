 -- Create contact_related_contacts table
CREATE TABLE contact_related_contacts (
    contact_id UUID NOT NULL,
    related_contact_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (contact_id, related_contact_id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
    FOREIGN KEY (related_contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_contact_related_contacts_contact_id ON contact_related_contacts(contact_id);
CREATE INDEX idx_contact_related_contacts_related_contact_id ON contact_related_contacts(related_contact_id);