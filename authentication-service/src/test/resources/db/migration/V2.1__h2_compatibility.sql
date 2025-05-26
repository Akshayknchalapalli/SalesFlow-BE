-- For H2 compatibility in tests
CREATE ALIAS IF NOT EXISTS auth.update_updated_at_column AS $$
void execute(java.sql.Connection connection) throws SQLException {
    // No-op for H2 since we'll handle timestamps in JPA
}
$$;

CREATE ALIAS IF NOT EXISTS update_users_updated_at AS $$
void execute(java.sql.Connection connection) throws SQLException {
    // No-op for H2
}
$$;

CREATE ALIAS IF NOT EXISTS update_tokens_updated_at AS $$
void execute(java.sql.Connection connection) throws SQLException {
    // No-op for H2
}
$$;

-- Create a procedure that checks if a role exists before inserting
-- This is H2's way of simulating "INSERT ... ON CONFLICT DO NOTHING"
CREATE ALIAS IF NOT EXISTS INSERT_ROLE_IF_NOT_EXISTS AS $$
void insertRoleIfNotExists(Connection conn, String roleName) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement(
        "MERGE INTO auth.roles (name) KEY (name) VALUES (?)")) {
        stmt.setString(1, roleName);
        stmt.executeUpdate();
    }
}
$$;

-- Insert the default roles for test database
CALL INSERT_ROLE_IF_NOT_EXISTS('ROLE_USER');
CALL INSERT_ROLE_IF_NOT_EXISTS('ROLE_ADMIN');
CALL INSERT_ROLE_IF_NOT_EXISTS('ROLE_TENANT_ADMIN');