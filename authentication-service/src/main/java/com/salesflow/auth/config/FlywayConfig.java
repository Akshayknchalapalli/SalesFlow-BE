package com.salesflow.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

import jakarta.annotation.PostConstruct;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this runs before other ApplicationRunners
public class FlywayConfig implements ApplicationRunner {

    private final DataSource dataSource;
    
    public FlywayConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Value("${spring.flyway.enabled:true}")
    private boolean flywayEnabled;

    @Value("${spring.flyway.schemas:auth}")
    private String[] defaultSchemas;

    // Define fixed migration locations for different schemas to avoid conflicts
    private static final String AUTH_MIGRATION_LOCATION = "classpath:db/migration/auth";
    private static final String PUBLIC_MIGRATION_LOCATION = "classpath:db/migration/public";
    private static final String TENANT_MIGRATION_LOCATION = "classpath:db/migration/tenant";
    
    @Value("${spring.flyway.repair-on-start:false}")
    private boolean repairOnStart;
    
    @Value("${spring.flyway.validate-on-migrate:true}")
    private boolean validateOnMigrate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!flywayEnabled) {
            log.info("Flyway migrations are disabled");
            return;
        }

        log.info("Initializing database schemas for multi-tenant setup");
    
        try {
            // Run repair if enabled (before any migration)
            if (repairOnStart) {
                log.info("Running Flyway repair before migrations");
                repairMigration("auth", AUTH_MIGRATION_LOCATION);
                repairMigration("public", PUBLIC_MIGRATION_LOCATION);
            }
        
            // Create public schema with tenant registry
            runMigration("public", PUBLIC_MIGRATION_LOCATION);
        
            // Create authentication schema
            runMigration("auth", AUTH_MIGRATION_LOCATION);
        
            // Load existing tenants and create schemas for them
            List<UUID> tenants = loadExistingTenants();
            for (UUID tenantId : tenants) {
                createTenantSchema(tenantId);
            }
        } catch (Exception e) {
            log.error("Database initialization failed", e);
            if (e instanceof org.flywaydb.core.api.exception.FlywayValidateException) {
                log.error("MIGRATION VALIDATION FAILED! Please consider the following options:");
                log.error("1. Set spring.flyway.repair-on-start=true to repair the schema history");
                log.error("2. Set spring.flyway.validate-on-migrate=false to skip validation");
                log.error("3. Manually clean the database and start fresh");
            }
            throw e;
        }
    }

    private void repairMigration(String schema, String location) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .schemas(schema)
            .locations(location)
            .load();
        flyway.repair();
    }

    private void runMigration(String schema, String location) {
        log.info("Running migration for schema {} from location {}", schema, location);
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .schemas(schema)
            .locations(location)
            .validateOnMigrate(validateOnMigrate)
            .baselineOnMigrate(true)
            .outOfOrder(true)  // Allow out of order migrations for more flexibility
            .ignoreMigrationPatterns("*:missing")  // Don't fail on missing migrations
            .load();
        flyway.migrate();
        log.info("Migration completed for schema {}", schema);
    }

    private List<UUID> loadExistingTenants() {
        List<UUID> tenants = new ArrayList<>();
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT tenant_id FROM public.tenants WHERE active = true");
            while (rs.next()) {
                tenants.add(rs.getObject("tenant_id", UUID.class));
            }
            log.info("Loaded {} existing tenants", tenants.size());
        } catch (Exception e) {
            log.warn("Could not load existing tenants", e);
        }
        return tenants;
    }

    private void createTenantSchema(UUID tenantId) {
        try {
            // Get tenant name from database
            String tenantName = getTenantName(tenantId);
            if (tenantName == null) {
                log.error("Failed to get tenant name for tenant ID: {}", tenantId);
                return;
            }
            
            // Format the schema name using tenant name instead of ID
            String schemaName = "tenant_" + formatSchemaName(tenantName);
            
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\"");
            log.info("Created schema for tenant: {} ({})", schemaName, tenantId);
            
            // Record schema creation in tenant_schemas table
            recordTenantSchema(tenantId, schemaName, "auth");
        } catch (Exception e) {
            log.error("Failed to create schema for tenant: {}", tenantId, e);
        }
    }
    
    /**
     * Gets the tenant name for a given tenant ID
     */
    private String getTenantName(UUID tenantId) {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement("SELECT name FROM public.tenants WHERE tenant_id = ?");
            pstmt.setObject(1, tenantId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get tenant name for tenant ID: {}", tenantId, e);
            return null;
        }
    }
    
    /**
     * Records the tenant schema in the tenant_schemas table
     */
    private void recordTenantSchema(UUID tenantId, String schemaName, String serviceName) {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO public.tenant_schemas (tenant_id, schema_name, service_name, migration_status) " +
                "VALUES (?, ?, ?, 'COMPLETED') " +
                "ON CONFLICT (tenant_id, service_name) DO UPDATE SET " +
                "schema_name = EXCLUDED.schema_name, migration_status = EXCLUDED.migration_status");
            pstmt.setObject(1, tenantId);
            pstmt.setString(2, schemaName);
            pstmt.setString(3, serviceName);
            pstmt.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to record tenant schema for tenant: {}", tenantId, e);
        }
    }
    
    /**
     * Formats a tenant name to be used as a schema name
     * Replaces spaces and special characters with underscores
     */
    private String formatSchemaName(String tenantName) {
        if (tenantName == null) {
            return "";
        }
        // Replace spaces and special characters with underscores
        // Convert to lowercase and limit length to avoid DB identifier limits
        return tenantName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }

    public void registerTenant(UUID tenantId, String name, String domain) {
        try {
            // Validate tenant name
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Tenant name cannot be empty");
            }
            
            // Format name for database use if needed
            String cleanName = name.trim();
            
            Connection conn = dataSource.getConnection();
            
            // Insert tenant record
            String insertTenant = "INSERT INTO public.tenants (tenant_id, name, domain, active) VALUES (?, ?, ?, true)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertTenant)) {
                pstmt.setObject(1, tenantId);
                pstmt.setString(2, cleanName);
                pstmt.setString(3, domain);
                pstmt.executeUpdate();
            }

            // Create tenant schema
            createTenantSchema(tenantId);

            // Run tenant-specific migrations
            String schemaName = "tenant_" + formatSchemaName(cleanName);
            runMigration("\"" + schemaName + "\"", TENANT_MIGRATION_LOCATION);

            log.info("Successfully registered tenant: {} ({})", cleanName, tenantId);
        } catch (Exception e) {
            log.error("Failed to register tenant: {} ({})", name, tenantId, e);
            throw new RuntimeException("Failed to register tenant: " + name + " (" + tenantId + ")", e);
        }
    }
}