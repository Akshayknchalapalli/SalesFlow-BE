-- Add check constraints
ALTER TABLE users
    ADD CONSTRAINT chk_username_length CHECK (length(username) >= 3 AND length(username) <= 50),
    ADD CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    ADD CONSTRAINT chk_tenant_id_format CHECK (tenant_id ~* '^[a-zA-Z0-9-_]+$' AND length(tenant_id) >= 3 AND length(tenant_id) <= 50);

-- Add additional indexes for common queries
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_tokens_expiry ON tokens(expiry_date);
CREATE INDEX IF NOT EXISTS idx_tokens_revoked ON tokens(revoked);

-- Add trigger for updating updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tokens_updated_at
    BEFORE UPDATE ON tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column(); 