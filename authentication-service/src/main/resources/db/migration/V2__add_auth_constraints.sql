-- Add check constraints
ALTER TABLE authentication.users
    ADD CONSTRAINT chk_username_length CHECK (char_length(username) BETWEEN 3 AND 50),
    ADD CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    ADD CONSTRAINT chk_tenant_id_format CHECK (tenant_id ~* '^[a-zA-Z0-9_-]+$');

-- Add additional indexes for common queries
CREATE INDEX IF NOT EXISTS idx_users_email ON authentication.users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON authentication.users(username);
CREATE INDEX IF NOT EXISTS idx_tokens_expiry ON authentication.tokens(expiry_date);
CREATE INDEX IF NOT EXISTS idx_tokens_revoked ON authentication.tokens(revoked);

