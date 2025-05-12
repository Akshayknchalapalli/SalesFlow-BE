-- Create contacts table
CREATE TABLE IF NOT EXISTS contacts (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(255),
    company_name VARCHAR(255),
    job_title VARCHAR(255),
    stage VARCHAR(50),
    team_id VARCHAR(255),
    region_id VARCHAR(255),
    owner_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL
);

-- Create contact_addresses table
CREATE TABLE IF NOT EXISTS contact_addresses (
    contact_id UUID REFERENCES contacts(id),
    type VARCHAR(50),
    street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    postal_code VARCHAR(255),
    country VARCHAR(255),
    primary BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create contact_social_profiles table
CREATE TABLE IF NOT EXISTS contact_social_profiles (
    contact_id UUID REFERENCES contacts(id),
    platform VARCHAR(50),
    profile_url VARCHAR(255),
    username VARCHAR(255),
    is_verified BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_contacts_email ON contacts(email);
CREATE INDEX IF NOT EXISTS idx_contacts_owner_id ON contacts(owner_id);
CREATE INDEX IF NOT EXISTS idx_contacts_team_id ON contacts(team_id);
CREATE INDEX IF NOT EXISTS idx_contacts_region_id ON contacts(region_id);
CREATE INDEX IF NOT EXISTS idx_contact_addresses_contact_id ON contact_addresses(contact_id);
CREATE INDEX IF NOT EXISTS idx_contact_social_profiles_contact_id ON contact_social_profiles(contact_id);

-- Add comments to tables and columns for better documentation
COMMENT ON TABLE contacts IS 'Stores contact information including personal and professional details';
COMMENT ON TABLE contact_addresses IS 'Stores multiple addresses associated with a contact';
COMMENT ON TABLE contact_social_profiles IS 'Stores social media profiles associated with a contact';

COMMENT ON COLUMN contacts.id IS 'Unique identifier for the contact';
COMMENT ON COLUMN contacts.first_name IS 'Contact''s first name';
COMMENT ON COLUMN contacts.last_name IS 'Contact''s last name';
COMMENT ON COLUMN contacts.email IS 'Contact''s email address (unique)';
COMMENT ON COLUMN contacts.phone IS 'Contact''s phone number';
COMMENT ON COLUMN contacts.company_name IS 'Contact''s company name';
COMMENT ON COLUMN contacts.job_title IS 'Contact''s job title';
COMMENT ON COLUMN contacts.stage IS 'Current stage of the contact in the sales pipeline';
COMMENT ON COLUMN contacts.team_id IS 'ID of the team that owns this contact';
COMMENT ON COLUMN contacts.region_id IS 'ID of the region this contact belongs to';
COMMENT ON COLUMN contacts.owner_id IS 'ID of the user who owns this contact';
COMMENT ON COLUMN contacts.created_at IS 'Timestamp when the contact was created';
COMMENT ON COLUMN contacts.updated_at IS 'Timestamp when the contact was last updated';
COMMENT ON COLUMN contacts.created_by IS 'User ID who created the contact';
COMMENT ON COLUMN contacts.updated_by IS 'User ID who last updated the contact';
COMMENT ON COLUMN contacts.version IS 'Version number for optimistic locking';

COMMENT ON COLUMN contact_addresses.contact_id IS 'Foreign key reference to contacts table';
COMMENT ON COLUMN contact_addresses.type IS 'Type of address (WORK, HOME, etc.)';
COMMENT ON COLUMN contact_addresses.street IS 'Street address';
COMMENT ON COLUMN contact_addresses.city IS 'City name';
COMMENT ON COLUMN contact_addresses.state IS 'State or province';
COMMENT ON COLUMN contact_addresses.postal_code IS 'Postal or ZIP code';
COMMENT ON COLUMN contact_addresses.country IS 'Country name';
COMMENT ON COLUMN contact_addresses.primary IS 'Flag indicating if this is the primary address';
COMMENT ON COLUMN contact_addresses.created_at IS 'Timestamp when the address was created';
COMMENT ON COLUMN contact_addresses.updated_at IS 'Timestamp when the address was last updated';
COMMENT ON COLUMN contact_addresses.created_by IS 'User ID who created the address';
COMMENT ON COLUMN contact_addresses.updated_by IS 'User ID who last updated the address';

COMMENT ON COLUMN contact_social_profiles.contact_id IS 'Foreign key reference to contacts table';
COMMENT ON COLUMN contact_social_profiles.platform IS 'Social media platform (LINKEDIN, TWITTER, etc.)';
COMMENT ON COLUMN contact_social_profiles.profile_url IS 'URL of the social media profile';
COMMENT ON COLUMN contact_social_profiles.username IS 'Username on the social media platform';
COMMENT ON COLUMN contact_social_profiles.is_verified IS 'Flag indicating if the profile is verified';
COMMENT ON COLUMN contact_social_profiles.created_at IS 'Timestamp when the profile was created';
COMMENT ON COLUMN contact_social_profiles.updated_at IS 'Timestamp when the profile was last updated';
COMMENT ON COLUMN contact_social_profiles.created_by IS 'User ID who created the profile';
COMMENT ON COLUMN contact_social_profiles.updated_by IS 'User ID who last updated the profile';
COMMENT ON COLUMN contact_social_profiles.version IS 'Version number for optimistic locking'; 