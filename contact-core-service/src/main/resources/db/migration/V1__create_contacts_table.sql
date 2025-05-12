-- Create contacts table
CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    company_name VARCHAR(255),
    job_title VARCHAR(255),
    stage VARCHAR(20) NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    team_id VARCHAR(255),
    region_id VARCHAR(255),
    preferred_contact_method VARCHAR(50),
    preferred_contact_time VARCHAR(50),
    do_not_contact BOOLEAN DEFAULT FALSE,
    marketing_opt_in BOOLEAN DEFAULT FALSE,
    communication_language VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes
CREATE INDEX idx_contacts_owner_id ON contacts(owner_id);
CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_stage ON contacts(stage);
CREATE INDEX idx_contacts_company_name ON contacts(company_name);
CREATE INDEX idx_contacts_team_id ON contacts(team_id);
CREATE INDEX idx_contacts_region_id ON contacts(region_id); 