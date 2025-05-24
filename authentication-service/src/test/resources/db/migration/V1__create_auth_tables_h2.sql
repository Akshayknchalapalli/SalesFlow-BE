-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS authentication;

-- Create roles table
CREATE TABLE IF NOT EXISTS authentication.roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE IF NOT EXISTS authentication.users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles table
CREATE TABLE IF NOT EXISTS authentication.user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES authentication.users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES authentication.roles(id) ON DELETE CASCADE
);

-- Create tokens table
CREATE TABLE IF NOT EXISTS authentication.tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id BIGINT NOT NULL,
    revoked BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES authentication.users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON authentication.users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tokens_user_id ON authentication.tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_tokens_refresh_token ON authentication.tokens(refresh_token);

-- Procedure to insert roles safely in H2
CREATE ALIAS IF NOT EXISTS MERGE_ROLE AS $$
void mergeRole(java.sql.Connection conn, String roleName) throws java.sql.SQLException {
    java.sql.PreparedStatement ps = conn.prepareStatement(
        "MERGE INTO authentication.roles (name) KEY (name) VALUES (?)");
    ps.setString(1, roleName);
    ps.executeUpdate();
    ps.close();
}
$$;

-- Insert default roles
CALL MERGE_ROLE('ROLE_USER');
CALL MERGE_ROLE('ROLE_ADMIN');
CALL MERGE_ROLE('ROLE_TENANT_ADMIN');