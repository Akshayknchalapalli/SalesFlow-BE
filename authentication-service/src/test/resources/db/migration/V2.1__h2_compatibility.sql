-- For H2 compatibility in tests
CREATE ALIAS IF NOT EXISTS authentication.update_updated_at_column AS $$
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