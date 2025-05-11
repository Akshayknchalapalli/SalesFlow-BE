-- Create tags table
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    color_hex VARCHAR(7),
    owner_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create contact_tags junction table
CREATE TABLE contact_tags (
    contact_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (contact_id, tag_id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Create timeline_entries table
CREATE TABLE timeline_entries (
    id BIGSERIAL PRIMARY KEY,
    contact_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

-- Add team and region columns to contacts table
ALTER TABLE contacts
ADD COLUMN team_id VARCHAR(255),
ADD COLUMN region_id VARCHAR(255);

-- Create indexes
CREATE INDEX idx_tags_owner_id ON tags(owner_id);
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_timeline_entries_contact_id ON timeline_entries(contact_id);
CREATE INDEX idx_timeline_entries_type ON timeline_entries(type);
CREATE INDEX idx_contacts_team_id ON contacts(team_id);
CREATE INDEX idx_contacts_region_id ON contacts(region_id); 