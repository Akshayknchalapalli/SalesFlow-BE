-- Create tags table
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    color_hex VARCHAR(7),
    owner_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_tags_owner_id ON tags(owner_id);
CREATE INDEX idx_tags_name ON tags(name); 