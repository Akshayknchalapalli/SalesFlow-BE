-- Add audit columns to contact_tags table
ALTER TABLE contact_tags
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_by VARCHAR(255) NOT NULL DEFAULT 'system',
ADD COLUMN version BIGINT NOT NULL DEFAULT 0; 