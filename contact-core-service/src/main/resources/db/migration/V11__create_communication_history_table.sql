-- Create communication_history table
CREATE TABLE communication_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    activity_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL DEFAULT 'system',
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_communication_history_contact_id ON communication_history(contact_id);
CREATE INDEX idx_communication_history_activity_date ON communication_history(activity_date); 