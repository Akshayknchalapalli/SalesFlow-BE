package com.salesflow.contact.config.tenant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of Hibernate's MultiTenantConnectionProvider that switches schemas
 * for the tenant-specific operations.
 * 
 * This provider uses a single DataSource and changes the schema for each connection
 * based on the tenant identifier.
 */
@Component
public class ContactMultiTenantConnectionProvider implements org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider<String> {

    @Value("${multitenancy.schemas}")
    private String schemas;

    private final DataSource dataSource;

    public ContactMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = dataSource.getConnection();
        if (tenantIdentifier != null && !tenantIdentifier.equals("public")) {
            connection.setSchema("tenant_" + tenantIdentifier);
        } else {
            connection.setSchema("public");
        }
        return connection;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}