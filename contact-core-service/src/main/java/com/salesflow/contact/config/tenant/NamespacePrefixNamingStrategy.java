package com.salesflow.contact.config.tenant;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Custom physical naming strategy that prefixes table names with a namespace.
 * This is used to implement the multi-service schema organization within tenant schemas.
 * 
 * For example, with a namespace of "contact_data", a table named "contacts" would
 * be created as "contact_data.contacts" in the database.
 */
@Component
public class NamespacePrefixNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Value("${multitenancy.namespace}")
    private String namespace;
    
    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        // Skip namespace prefix for public schema tables
        if (name.getCanonicalName().startsWith("public.")) {
            return name;
        }

        // Add namespace prefix for tenant-specific tables
        String tableName = name.getCanonicalName();
        if (!tableName.startsWith(namespace + ".")) {
            tableName = namespace + "." + tableName;
        }
        return Identifier.toIdentifier(tableName);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }
    
    /**
     * Determines if a table should skip namespace prefixing
     */
    private boolean shouldSkipNamespace(String tableName) {
        // Skip tables that already have a namespace prefix
        if (tableName.contains(".")) {
            return true;
        }
        
        // Skip specific tables that should not have a namespace
        return tableName.equals("flyway_schema_history") || 
               tableName.equals("hibernate_sequence") ||
               tableName.startsWith("namespace_");
    }
    
    /**
     * Creates a new identifier with the same quoting semantics as the original
     */
    private Identifier getIdentifier(String text, boolean quoted, JdbcEnvironment jdbcEnvironment) {
        return jdbcEnvironment.getIdentifierHelper().toIdentifier(text, quoted);
    }
}