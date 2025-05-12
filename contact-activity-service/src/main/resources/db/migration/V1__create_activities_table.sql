CREATE TABLE IF NOT EXISTS activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(50),
    scheduled_time TIMESTAMP,
    completed_time TIMESTAMP,
    assigned_to VARCHAR(100),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contact FOREIGN KEY (contact_id) REFERENCES contacts(id)
);

CREATE INDEX idx_activities_contact_id ON activities(contact_id);
CREATE INDEX idx_activities_type ON activities(type);
CREATE INDEX idx_activities_status ON activities(status);
CREATE INDEX idx_activities_scheduled_time ON activities(scheduled_time);
CREATE INDEX idx_activities_assigned_to ON activities(assigned_to); 