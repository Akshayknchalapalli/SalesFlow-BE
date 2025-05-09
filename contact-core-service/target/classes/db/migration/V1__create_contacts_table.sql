CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    company_name VARCHAR(255),
    job_title VARCHAR(255),
    stage VARCHAR(20) NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    preferred_contact_method VARCHAR(50),
    preferred_contact_time VARCHAR(50),
    do_not_contact BOOLEAN DEFAULT FALSE,
    marketing_opt_in BOOLEAN DEFAULT FALSE,
    communication_language VARCHAR(10),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE contact_addresses (
    contact_id BIGINT NOT NULL,
    type VARCHAR(50),
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

CREATE TABLE contact_social_profiles (
    contact_id BIGINT NOT NULL,
    platform VARCHAR(50),
    profile_url VARCHAR(255),
    username VARCHAR(100),
    is_verified BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
);

CREATE INDEX idx_contacts_owner_id ON contacts(owner_id);
CREATE INDEX idx_contacts_email ON contacts(email);
CREATE INDEX idx_contacts_stage ON contacts(stage);
CREATE INDEX idx_contacts_company_name ON contacts(company_name); 