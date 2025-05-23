-- Add check constraints
ALTER TABLE authentication.users
    ADD CONSTRAINT chk_username_length CHECK (length(username) >= 3 AND length(username) <= 50),
    ADD CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    ADD CONSTRAINT chk_tenant_id_format CHECK (tenant_id ~* '^[a-zA-Z0-9_-]+$' AND length(tenant_id) >= 3 AND length(tenant_id) <= 50);

-- Add additional indexes for common queries
CREATE INDEX IF NOT EXISTS idx_users_email ON authentication.users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON authentication.users(username);
CREATE INDEX IF NOT EXISTS idx_tokens_expiry ON authentication.tokens(expiry_date);
CREATE INDEX IF NOT EXISTS idx_tokens_revoked ON authentication.tokens(revoked);

-- Add trigger for updating updated_at timestamp
CREATE OR REPLACE FUNCTION authentication.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON authentication.users
    FOR EACH ROW
    EXECUTE FUNCTION authentication.update_updated_at_column();

CREATE TRIGGER update_tokens_updated_at
    BEFORE UPDATE ON authentication.tokens
    FOR EACH ROW
    EXECUTE FUNCTION authentication.update_updated_at_column(); 