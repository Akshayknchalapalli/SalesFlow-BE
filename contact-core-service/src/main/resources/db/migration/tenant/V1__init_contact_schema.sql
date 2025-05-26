-- Initialize tenant-specific schema for contacts
-- This migration creates the basic tables needed for contact management

-- Contacts table - core entity for contact management
CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    mobile VARCHAR(50),
    company VARCHAR(100),
    title VARCHAR(100),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    lead_source VARCHAR(100),
    lead_status VARCHAR(50),
    lifecycle_stage VARCHAR(50),
    assigned_to VARCHAR(100),
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    custom_fields JSONB DEFAULT '{}'::jsonb
);

-- Create indexes for better performance
CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_company ON contacts(company);
CREATE INDEX idx_contacts_lead_status ON contacts(lead_status);
CREATE INDEX idx_contacts_lifecycle_stage ON contacts(lifecycle_stage);
CREATE INDEX idx_contacts_created_at ON contacts(created_at);
CREATE INDEX idx_contacts_is_deleted ON contacts(is_deleted);

-- Notes for contacts
CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    contact_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX idx_notes_contact_id ON notes(contact_id);

-- Tags for categorizing contacts
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Contact-Tag relationship (many-to-many)
CREATE TABLE contact_tags (
    contact_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (contact_id, tag_id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);

-- Timeline entries for contact activity
CREATE TABLE timeline_entries (
    id BIGSERIAL PRIMARY KEY,
    contact_id BIGINT NOT NULL,
    entry_type VARCHAR(50) NOT NULL, -- email, call, meeting, note, etc.
    title VARCHAR(255) NOT NULL,
    description TEXT,
    occurred_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    metadata JSONB DEFAULT '{}'::jsonb,
    FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX idx_timeline_entries_contact_id ON timeline_entries(contact_id);
CREATE INDEX idx_timeline_entries_entry_type ON timeline_entries(entry_type);
CREATE INDEX idx_timeline_entries_occurred_at ON timeline_entries(occurred_at);

-- Insert default tags
INSERT INTO tags (name, color) VALUES 
('Lead', '#FF9900'),
('Customer', '#00CC66'),
('Prospect', '#3399FF'),
('Partner', '#9966CC'),
('Vendor', '#FF6666');