-- V1__create_users_tables.sql
-- Creates the authentication service tables in the auth schema

-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS auth;

-- Create users table
CREATE TABLE IF NOT EXISTS auth.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    password_reset_token VARCHAR(100),
    password_reset_expires_at TIMESTAMP WITH TIME ZONE,
    email_verification_token VARCHAR(100),
    email_verified BOOLEAN DEFAULT FALSE,
    profile_image_url VARCHAR(255),
    CONSTRAINT users_username_format CHECK (username ~ '^[a-zA-Z0-9._-]+$'),
    CONSTRAINT users_email_format CHECK (email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$')
);

-- Create roles table
CREATE TABLE IF NOT EXISTS auth.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles join table
CREATE TABLE IF NOT EXISTS auth.user_roles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(50),
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES auth.roles(id) ON DELETE CASCADE
);

-- Create permissions table
CREATE TABLE IF NOT EXISTS auth.permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create role_permissions join table
CREATE TABLE IF NOT EXISTS auth.role_permissions (
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES auth.roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES auth.permissions(id) ON DELETE CASCADE
);

-- Create refresh tokens table
CREATE TABLE IF NOT EXISTS auth.refresh_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_reason VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE
);

-- Create authentication audit log
CREATE TABLE IF NOT EXISTS auth.auth_audit_log (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    username VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE SET NULL
);

-- Create indices for improved performance
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON auth.users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON auth.users(email);
CREATE INDEX IF NOT EXISTS idx_auth_audit_log_tenant_id ON auth.auth_audit_log(tenant_id);
CREATE INDEX IF NOT EXISTS idx_auth_audit_log_timestamp ON auth.auth_audit_log(timestamp);

-- Insert default roles
INSERT INTO auth.roles (name, description) 
VALUES 
    ('ROLE_ADMIN', 'System administrator with full access'),
    ('ROLE_TENANT_ADMIN', 'Tenant administrator with full access to tenant resources'),
    ('ROLE_USER', 'Regular user with standard permissions')
ON CONFLICT (name) DO NOTHING;

-- Insert default permissions
INSERT INTO auth.permissions (name, description)
VALUES
    ('user:read', 'View user information'),
    ('user:create', 'Create new users'),
    ('user:update', 'Update user information'),
    ('user:delete', 'Delete users'),
    ('contact:read', 'View contacts'),
    ('contact:create', 'Create new contacts'),
    ('contact:update', 'Update contact information'),
    ('contact:delete', 'Delete contacts'),
    ('activity:read', 'View activities'),
    ('activity:create', 'Create new activities'),
    ('activity:update', 'Update activities'),
    ('activity:delete', 'Delete activities')
ON CONFLICT (name) DO NOTHING;

-- Assign permissions to roles
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth.roles r, auth.permissions p
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth.roles r, auth.permissions p
WHERE r.name = 'ROLE_TENANT_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM auth.roles r, auth.permissions p
WHERE r.name = 'ROLE_USER' AND p.name IN ('contact:read', 'contact:create', 'contact:update', 'activity:read', 'activity:create', 'activity:update')
ON CONFLICT DO NOTHING;

-- Create default admin user (password: admin123)
INSERT INTO auth.users (username, email, password, tenant_id, email_verified, first_name, last_name)
VALUES ('admin', 'admin@system.com', '$2a$10$EqKMCKIw69ZdBZG1ouJlQeyIwZDPD9JDGJuJ8bMl.fE.z6JN.jUzq', 'system', TRUE, 'System', 'Administrator')
ON CONFLICT (username) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO auth.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth.users u, auth.roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;