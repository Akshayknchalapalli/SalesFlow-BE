-- V1__create_tenant_tables.sql
-- Creates the tenant-specific tables for authentication data

-- Create tenant_settings table
CREATE TABLE IF NOT EXISTS tenant_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create user_preferences table for storing user-specific preferences
CREATE TABLE IF NOT EXISTS user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    preference_key VARCHAR(100) NOT NULL,
    preference_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, preference_key)
);

-- Create index on user_id for fast lookups
CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);

-- Create login_history table for tracking user logins
CREATE TABLE IF NOT EXISTS login_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    login_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_info TEXT,
    success BOOLEAN NOT NULL
);

-- Create index on user_id for fast lookups
CREATE INDEX idx_login_history_user_id ON login_history(user_id);
CREATE INDEX idx_login_history_timestamp ON login_history(login_timestamp);

-- Create password_history table for tracking password changes
CREATE TABLE IF NOT EXISTS password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create index on user_id for fast lookups
CREATE INDEX idx_password_history_user_id ON password_history(user_id);

-- Create user_sessions table for tracking active sessions
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_active_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_session_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);

-- Insert default settings
INSERT INTO tenant_settings (setting_key, setting_value, description)
VALUES 
    ('password_policy.min_length', '8', 'Minimum password length'),
    ('password_policy.require_uppercase', 'true', 'Require at least one uppercase letter'),
    ('password_policy.require_lowercase', 'true', 'Require at least one lowercase letter'),
    ('password_policy.require_digit', 'true', 'Require at least one digit'),
    ('password_policy.require_special', 'true', 'Require at least one special character'),
    ('password_policy.max_age_days', '90', 'Maximum password age in days'),
    ('login.max_attempts', '5', 'Maximum failed login attempts before lockout'),
    ('login.lockout_duration_minutes', '30', 'Account lockout duration in minutes'),
    ('session.timeout_minutes', '30', 'Session timeout in minutes of inactivity'),
    ('session.max_duration_hours', '12', 'Maximum session duration in hours')
ON CONFLICT (setting_key) DO NOTHING;