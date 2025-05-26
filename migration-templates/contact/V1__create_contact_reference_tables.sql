-- V1__create_contact_reference_tables.sql
-- Creates the contact service reference tables in the contact schema

-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS contact;

-- Create contact types reference table
CREATE TABLE IF NOT EXISTS contact.contact_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon VARCHAR(50),
    color VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    is_system BOOLEAN DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create contact status reference table
CREATE TABLE IF NOT EXISTS contact.contact_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    color VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    is_system BOOLEAN DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create tag categories reference table
CREATE TABLE IF NOT EXISTS contact.tag_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    color VARCHAR(20),
    icon VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create industry types reference table
CREATE TABLE IF NOT EXISTS contact.industry_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create country codes reference table
CREATE TABLE IF NOT EXISTS contact.country_codes (
    id SERIAL PRIMARY KEY,
    code VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    phone_code VARCHAR(10),
    flag_emoji VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create email templates for contact service
CREATE TABLE IF NOT EXISTS contact.email_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    is_html BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default contact types
INSERT INTO contact.contact_types (name, description, icon, color, is_default, is_system, display_order)
VALUES 
    ('Person', 'Individual contact', 'person', '#4CAF50', TRUE, TRUE, 1),
    ('Company', 'Business or organization', 'business', '#2196F3', FALSE, TRUE, 2),
    ('Lead', 'Potential customer', 'trending_up', '#FFC107', FALSE, TRUE, 3),
    ('Customer', 'Active customer', 'star', '#9C27B0', FALSE, TRUE, 4),
    ('Partner', 'Business partner', 'handshake', '#FF5722', FALSE, TRUE, 5),
    ('Vendor', 'Service or product provider', 'local_shipping', '#795548', FALSE, TRUE, 6)
ON CONFLICT (name) DO NOTHING;

-- Insert default contact statuses
INSERT INTO contact.contact_statuses (name, description, color, is_default, is_system, display_order)
VALUES 
    ('Active', 'Currently active contact', '#4CAF50', TRUE, TRUE, 1),
    ('Inactive', 'Inactive or dormant contact', '#9E9E9E', FALSE, TRUE, 2),
    ('Lead', 'Potential lead not yet contacted', '#FFC107', FALSE, TRUE, 3),
    ('Qualified', 'Qualified lead', '#2196F3', FALSE, TRUE, 4),
    ('Customer', 'Converted to customer', '#9C27B0', FALSE, TRUE, 5),
    ('Do Not Contact', 'Do not contact this person', '#F44336', FALSE, TRUE, 6)
ON CONFLICT (name) DO NOTHING;

-- Insert default tag categories
INSERT INTO contact.tag_categories (name, description, color, icon)
VALUES 
    ('Interest', 'Areas of interest', '#4CAF50', 'favorite'),
    ('Source', 'Lead source', '#2196F3', 'source'),
    ('Priority', 'Contact priority', '#FFC107', 'flag'),
    ('Department', 'Department or division', '#9C27B0', 'groups'),
    ('Region', 'Geographic region', '#FF5722', 'public'),
    ('Status', 'Contact status', '#795548', 'info')
ON CONFLICT (name) DO NOTHING;

-- Insert sample industry types
INSERT INTO contact.industry_types (name, description)
VALUES 
    ('Technology', 'Software, hardware, and IT services'),
    ('Healthcare', 'Medical services and healthcare'),
    ('Finance', 'Banking, insurance, and financial services'),
    ('Education', 'Schools, universities, and educational services'),
    ('Manufacturing', 'Production and manufacturing'),
    ('Retail', 'Retail stores and e-commerce'),
    ('Professional Services', 'Consulting, legal, and other professional services'),
    ('Real Estate', 'Property management and real estate'),
    ('Hospitality', 'Hotels, restaurants, and tourism'),
    ('Media', 'Publishing, broadcasting, and entertainment')
ON CONFLICT (name) DO NOTHING;

-- Create system settings for contact service
CREATE TABLE IF NOT EXISTS contact.system_settings (
    id SERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    is_encrypted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default system settings
INSERT INTO contact.system_settings (setting_key, setting_value, setting_type, description)
VALUES
    ('default_contact_type', '1', 'INTEGER', 'Default contact type ID for new contacts'),
    ('default_contact_status', '1', 'INTEGER', 'Default status ID for new contacts'),
    ('enable_duplicate_detection', 'true', 'BOOLEAN', 'Enable duplicate contact detection'),
    ('duplicate_detection_threshold', '80', 'INTEGER', 'Similarity percentage threshold for duplicate detection'),
    ('max_contacts_per_import', '1000', 'INTEGER', 'Maximum contacts allowed per import operation')
ON CONFLICT (setting_key) DO NOTHING;