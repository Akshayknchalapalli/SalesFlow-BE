-- V1__create_activity_tables.sql
-- Creates the tenant-specific activity tables

-- Create activity_data namespace (table prefix grouping)
-- Note: Tables will be created in the tenant schema with this prefix

-- Create activities table
CREATE TABLE activity_data.activities (
    id SERIAL PRIMARY KEY,
    activity_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    contact_id INTEGER,
    related_to VARCHAR(100),
    related_id INTEGER,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    is_all_day BOOLEAN DEFAULT FALSE,
    location VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PLANNED',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    reminder_time INTEGER,
    reminder_sent BOOLEAN DEFAULT FALSE,
    outcome TEXT,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    completed_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create activity participants table
CREATE TABLE activity_data.activity_participants (
    id SERIAL PRIMARY KEY,
    activity_id INTEGER NOT NULL,
    contact_id INTEGER,
    user_id VARCHAR(100),
    participant_type VARCHAR(50) NOT NULL,
    response_status VARCHAR(50) DEFAULT 'PENDING',
    notification_sent BOOLEAN DEFAULT FALSE,
    notification_sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activity_data.activities(id) ON DELETE CASCADE
);

-- Create activity comments table
CREATE TABLE activity_data.activity_comments (
    id SERIAL PRIMARY KEY,
    activity_id INTEGER NOT NULL,
    comment_text TEXT NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activity_data.activities(id) ON DELETE CASCADE
);

-- Create activity attachments table
CREATE TABLE activity_data.activity_attachments (
    id SERIAL PRIMARY KEY,
    activity_id INTEGER NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size INTEGER,
    file_path VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_public BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (activity_id) REFERENCES activity_data.activities(id) ON DELETE CASCADE
);

-- Create activity reminders table
CREATE TABLE activity_data.activity_reminders (
    id SERIAL PRIMARY KEY,
    activity_id INTEGER NOT NULL,
    reminder_time INTEGER NOT NULL,
    notification_type VARCHAR(50) DEFAULT 'EMAIL',
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP WITH TIME ZONE,
    recipient VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activity_data.activities(id) ON DELETE CASCADE
);

-- Create activity custom fields table
CREATE TABLE activity_data.activity_custom_fields (
    id SERIAL PRIMARY KEY,
    activity_id INTEGER NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_value TEXT,
    field_type VARCHAR(50) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activity_data.activities(id) ON DELETE CASCADE,
    CONSTRAINT activity_custom_fields_unique UNIQUE (activity_id, field_name)
);

-- Create activity templates table
CREATE TABLE activity_data.activity_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    activity_type VARCHAR(50) NOT NULL,
    subject_template VARCHAR(255) NOT NULL,
    description_template TEXT,
    duration_minutes INTEGER DEFAULT 30,
    is_all_day BOOLEAN DEFAULT FALSE,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT activity_templates_name_unique UNIQUE (name)
);

-- Create activity type settings table
CREATE TABLE activity_data.activity_type_settings (
    id SERIAL PRIMARY KEY,
    activity_type VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    is_enabled BOOLEAN DEFAULT TRUE,
    requires_outcome BOOLEAN DEFAULT FALSE,
    requires_contact BOOLEAN DEFAULT FALSE,
    default_duration_minutes INTEGER DEFAULT 30,
    default_reminder_minutes INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT activity_type_settings_unique UNIQUE (activity_type)
);

-- Create recurring activities table
CREATE TABLE activity_data.recurring_activities (
    id SERIAL PRIMARY KEY,
    parent_activity_id INTEGER NOT NULL,
    recurrence_pattern VARCHAR(50) NOT NULL,
    recurrence_value INTEGER NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    max_occurrences INTEGER,
    days_of_week VARCHAR(20),
    day_of_month INTEGER,
    month_of_year INTEGER,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_generated_date TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (parent_activity_id) REFERENCES activity_data.activities(id) ON DELETE CASCADE
);

-- Insert default activity type settings
INSERT INTO activity_data.activity_type_settings 
(activity_type, icon, color, is_enabled, requires_outcome, requires_contact, default_duration_minutes, default_reminder_minutes)
VALUES
    ('CALL', 'phone', '#4CAF50', TRUE, TRUE, TRUE, 15, 5),
    ('MEETING', 'groups', '#2196F3', TRUE, TRUE, TRUE, 60, 15),
    ('EMAIL', 'email', '#FF9800', TRUE, FALSE, TRUE, 10, NULL),
    ('TASK', 'assignment', '#9C27B0', TRUE, TRUE, FALSE, 30, 60),
    ('DEADLINE', 'event', '#F44336', TRUE, FALSE, FALSE, 0, 1440),
    ('NOTE', 'note', '#607D8B', TRUE, FALSE, TRUE, 0, NULL);

-- Create indices for better performance
CREATE INDEX idx_activities_type ON activity_data.activities(activity_type);
CREATE INDEX idx_activities_contact ON activity_data.activities(contact_id);
CREATE INDEX idx_activities_related ON activity_data.activities(related_to, related_id);
CREATE INDEX idx_activities_dates ON activity_data.activities(start_date, end_date);
CREATE INDEX idx_activities_status ON activity_data.activities(status);
CREATE INDEX idx_activities_created_by ON activity_data.activities(created_by);
CREATE INDEX idx_activity_participants_activity ON activity_data.activity_participants(activity_id);
CREATE INDEX idx_activity_participants_contact ON activity_data.activity_participants(contact_id);
CREATE INDEX idx_activity_participants_user ON activity_data.activity_participants(user_id);