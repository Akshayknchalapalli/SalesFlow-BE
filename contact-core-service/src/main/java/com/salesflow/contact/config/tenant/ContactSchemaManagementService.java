package com.salesflow.contact.config.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for managing tenant-specific schemas and tables.
 * This includes creating new tenant schemas and initializing their tables.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactSchemaManagementService {

    @Value("${multitenancy.namespace}")
    private String namespace;

    @Value("${multitenancy.schemas}")
    private String schemas;

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new tenant schema and initializes its tables.
     *
     * @param tenantId the tenant identifier
     */
    @Transactional
    public void createTenantSchema(UUID tenantId) {
        String schemaName = "tenant_" + tenantId.toString();
        log.info("Creating schema for tenant: {}", schemaName);

        // Create the tenant schema
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);

        // Create the contact_data namespace in the tenant schema
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName + "." + namespace);

        // Initialize tables in the tenant schema
        initializeTenantTables(schemaName);
    }

    /**
     * Initializes all required tables in the tenant schema.
     *
     * @param schemaName the tenant schema name
     */
    private void initializeTenantTables(String schemaName) {
        List<String> tables = Arrays.asList(
            "contacts",
            "contact_addresses",
            "contact_social_profiles",
            "contact_tags",
            "notes",
            "timeline_entries"
        );

        for (String table : tables) {
            String fullTableName = schemaName + "." + namespace + "." + table;
            log.info("Initializing table: {}", fullTableName);
            // Add table creation logic here
        }
    }

    /**
     * Drops a tenant schema and all its contents.
     *
     * @param tenantId the tenant identifier
     */
    @Transactional
    public void dropTenantSchema(UUID tenantId) {
        String schemaName = "tenant_" + tenantId.toString();
        log.info("Dropping schema for tenant: {}", schemaName);
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
    }

    /**
     * Checks if a tenant schema exists.
     *
     * @param tenantId the tenant identifier
     * @return true if the schema exists, false otherwise
     */
    public boolean schemaExists(UUID tenantId) {
        String schemaName = "tenant_" + tenantId.toString();
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?",
            Integer.class,
            schemaName
        );
        return count != null && count > 0;
    }
}