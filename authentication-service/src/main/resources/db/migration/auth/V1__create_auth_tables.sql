-- V1__create_auth_tables.sql
-- Creates the authentication tables in the auth schema

-- Create roles table
CREATE TABLE IF NOT EXISTS auth.roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create default roles
INSERT INTO auth.roles (name, description) 
VALUES 
    ('ROLE_ADMIN', 'System administrator with full access'),
    ('ROLE_TENANT_ADMIN', 'Tenant administrator with full access to tenant resources'),
    ('ROLE_USER', 'Regular user with standard access')
ON CONFLICT (name) DO NOTHING;

-- Create users table
CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_users_tenant_id ON auth.users(tenant_id);
CREATE INDEX idx_users_email ON auth.users(email);

-- Create user_roles table for many-to-many relationship
CREATE TABLE IF NOT EXISTS auth.user_roles (
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES auth.roles(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

-- Create tokens table for password reset and other tokens
CREATE TABLE IF NOT EXISTS auth.tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create index on token for fast lookups
CREATE INDEX idx_tokens_token ON auth.tokens(token);
CREATE INDEX idx_tokens_user_id ON auth.tokens(user_id);

-- Create an admin user (password: admin123)
INSERT INTO auth.users (username, password, email, tenant_id, enabled)
VALUES ('admin', '$2a$10$uK5UGJnVaxZKCFGdj9bMUuC/L3/XNRh4QEbCQXA1HUlD.Ip1d74/2', 'admin@salesflow.com', 
        '00000000-0000-0000-0000-000000000000', true)
ON CONFLICT (username) DO NOTHING;

-- Assign ROLE_ADMIN to the admin user
INSERT INTO auth.user_roles (user_id, role_id)
SELECT 
    (SELECT id FROM auth.users WHERE username = 'admin'),
    (SELECT id FROM auth.roles WHERE name = 'ROLE_ADMIN')
WHERE 
    EXISTS (SELECT 1 FROM auth.users WHERE username = 'admin') AND
    EXISTS (SELECT 1 FROM auth.roles WHERE name = 'ROLE_ADMIN')
ON CONFLICT (user_id, role_id) DO NOTHING;