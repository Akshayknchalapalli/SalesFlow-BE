-- V1__create_contact_tables.sql
-- Creates the tenant-specific contact tables

-- Create contact_data namespace (table prefix grouping)
-- Note: Tables will be created in the tenant schema with this prefix

-- Create contacts table
CREATE TABLE contact_data.contacts (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(100),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    company_name VARCHAR(150),
    title VARCHAR(100),
    email VARCHAR(150),
    email_verified BOOLEAN DEFAULT FALSE,
    phone VARCHAR(30),
    mobile VARCHAR(30),
    address_line1 VARCHAR(150),
    address_line2 VARCHAR(150),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    country_code VARCHAR(2),
    contact_type_id INTEGER,
    status_id INTEGER,
    industry_id INTEGER,
    source VARCHAR(100),
    source_details TEXT,
    assigned_to VARCHAR(100),
    rating INTEGER,
    last_contact_date TIMESTAMP WITH TIME ZONE,
    next_follow_up_date TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    website VARCHAR(255),
    linkedin_url VARCHAR(255),
    twitter_handle VARCHAR(100),
    facebook_url VARCHAR(255),
    instagram_handle VARCHAR(100),
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    is_deleted BOOLEAN DEFAULT FALSE,
    is_favorite BOOLEAN DEFAULT FALSE,
    avatar_url VARCHAR(255),
    CONSTRAINT contacts_email_unique UNIQUE (email)
);

-- Create contact tags table
CREATE TABLE contact_data.contact_tags (
    id SERIAL PRIMARY KEY,
    contact_id INTEGER NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    tag_category VARCHAR(50),
    tag_color VARCHAR(20),
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE,
    CONSTRAINT contact_tags_unique UNIQUE (contact_id, tag_name)
);

-- Create contact custom fields table
CREATE TABLE contact_data.contact_custom_fields (
    id SERIAL PRIMARY KEY,
    contact_id INTEGER NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_value TEXT,
    field_type VARCHAR(50) NOT NULL,
    is_searchable BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE,
    CONSTRAINT contact_custom_fields_unique UNIQUE (contact_id, field_name)
);

-- Create contact addresses table (for multiple addresses)
CREATE TABLE contact_data.contact_addresses (
    id SERIAL PRIMARY KEY,
    contact_id INTEGER NOT NULL,
    address_type VARCHAR(50) NOT NULL,
    address_line1 VARCHAR(150),
    address_line2 VARCHAR(150),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    country_code VARCHAR(2),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE
);

-- Create contact emails table (for multiple emails)
CREATE TABLE contact_data.contact_emails (
    id SERIAL PRIMARY KEY,
    contact_id INTEGER NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    email VARCHAR(150) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    is_primary BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(100),
    verification_sent_at TIMESTAMP WITH TIME ZONE,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE,
    CONSTRAINT contact_emails_unique UNIQUE (contact_id, email)
);

-- Create contact phones table (for multiple phone numbers)
CREATE TABLE contact_data.contact_phones (
    id SERIAL PRIMARY KEY,
    contact_id INTEGER NOT NULL,
    phone_type VARCHAR(50) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    country_code VARCHAR(5),
    extension VARCHAR(20),
    is_primary BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE
);

-- Create contact notes table
CREATE TABLE contact_data.contact_notes (
    id SERIAL PRIMARY KEY,
    contact_id INTEGER NOT NULL,
    note_text TEXT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE
);

-- Create contact groups table
CREATE TABLE contact_data.contact_groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_dynamic BOOLEAN DEFAULT FALSE,
    filter_criteria JSONB,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT contact_groups_name_unique UNIQUE (name)
);

-- Create contact group members table
CREATE TABLE contact_data.contact_group_members (
    id SERIAL PRIMARY KEY,
    group_id INTEGER NOT NULL,
    contact_id INTEGER NOT NULL,
    added_by VARCHAR(100) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES contact_data.contact_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE,
    CONSTRAINT contact_group_members_unique UNIQUE (group_id, contact_id)
);

-- Create contact relationships table
CREATE TABLE contact_data.contact_relationships (
    id SERIAL PRIMARY KEY,
    from_contact_id INTEGER NOT NULL,
    to_contact_id INTEGER NOT NULL,
    relationship_type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE,
    FOREIGN KEY (to_contact_id) REFERENCES contact_data.contacts(id) ON DELETE CASCADE,
    CONSTRAINT contact_relationships_unique UNIQUE (from_contact_id, to_contact_id, relationship_type)
);

-- Create indices for better performance
CREATE INDEX idx_contacts_email ON contact_data.contacts(email);
CREATE INDEX idx_contacts_name ON contact_data.contacts(last_name, first_name);
CREATE INDEX idx_contacts_company ON contact_data.contacts(company_name);
CREATE INDEX idx_contacts_type ON contact_data.contacts(contact_type_id);
CREATE INDEX idx_contacts_status ON contact_data.contacts(status_id);
CREATE INDEX idx_contacts_assigned_to ON contact_data.contacts(assigned_to);
CREATE INDEX idx_contacts_created_at ON contact_data.contacts(created_at);
CREATE INDEX idx_contacts_follow_up ON contact_data.contacts(next_follow_up_date);
CREATE INDEX idx_contact_tags_name ON contact_data.contact_tags(tag_name);
CREATE INDEX idx_contact_custom_fields_searchable ON contact_data.contact_custom_fields(contact_id) WHERE is_searchable = TRUE;