package com.salesflow.common.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for managing database schemas in a multi-tenant,
 * multi-service environment.
 */
@Component
public class SchemaManagementUtility {
    private static final Logger log = LoggerFactory.getLogger(SchemaManagementUtility.class);
    
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    
    @Value("${spring.application.name}")
    private String serviceName;
    
    @Value("${multitenancy.migration.locations.public:classpath:db/migration/public}")
    private String publicMigrationLocations;
    
    @Value("${multitenancy.migration.locations.service:classpath:db/migration/${spring.application.name}}")
    private String serviceMigrationLocations;
    
    @Value("${multitenancy.migration.locations.tenant:classpath:db/migration/tenant/${spring.application.name}}")
    private String tenantMigrationLocations;
    
    @Value("${multitenancy.tables.prefix:#{null}}")
    private String tablePrefix;
    
    @Autowired
    public SchemaManagementUtility(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Initialize the database schemas required for this service
     */
    public void initializeServiceSchemas() {
        log.info("Initializing service schemas for {}", serviceName);
        
        try {
            // Create public schema if it doesn't exist (should already exist, but just in case)
            createSchemaIfNotExists("public");
            
            // Apply migrations to public schema
            applyMigrations("public", publicMigrationLocations);
            
            // Create service-specific schema
            String serviceSchema = getServiceSchemaName();
            createSchemaIfNotExists(serviceSchema);
            
            // Apply migrations to service schema
            applyMigrations(serviceSchema, serviceMigrationLocations);
            
            // Initialize existing tenant schemas
            initializeExistingTenantSchemas();
            
            log.info("Schema initialization completed successfully");
        } catch (Exception e) {
            log.error("Error initializing service schemas", e);
            throw new RuntimeException("Failed to initialize database schemas", e);
        }
    }
    
    /**
     * Initialize schemas for all existing tenants
     */
    private void initializeExistingTenantSchemas() {
        List<String> tenants = getAllTenants();
        log.info("Found {} existing tenants", tenants.size());
        
        for (String tenantId : tenants) {
            initializeTenantSchema(tenantId);
        }
    }
    
    /**
     * Initialize schema for a specific tenant
     */
    @Transactional
    public void initializeTenantSchema(String tenantId) {
        log.info("Initializing tenant schema for tenant: {}", tenantId);
        
        try {
            // Create tenant schema if it doesn't exist
            String tenantSchema = getTenantSchemaName(tenantId);
            createSchemaIfNotExists(tenantSchema);
            
            // Create necessary namespaces within the tenant schema
            createNamespacesForTenant(tenantSchema);
            
            // Apply tenant-specific migrations
            applyMigrations(tenantSchema, tenantMigrationLocations);
            
            // Register the schema creation in tenant_schemas table
            registerTenantSchema(tenantId, tenantSchema);
            
            log.info("Tenant schema initialized successfully: {}", tenantSchema);
        } catch (Exception e) {
            log.error("Error initializing tenant schema for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to initialize tenant schema", e);
        }
    }
    
    /**
     * Get all active tenants from the registry
     */
    public List<String> getAllTenants() {
        List<String> tenants = new ArrayList<>();
        
        try {
            jdbcTemplate.query(
                "SELECT tenant_id FROM public.tenants WHERE active = true",
                rs -> {
                    while (rs.next()) {
                        tenants.add(rs.getString("tenant_id"));
                    }
                }
            );
        } catch (Exception e) {
            log.error("Error retrieving tenants", e);
            // Return empty list if table doesn't exist yet or other error
        }
        
        return tenants;
    }
    
    /**
     * Create a schema if it doesn't exist
     */
    private void createSchemaIfNotExists(String schemaName) {
        log.debug("Creating schema if it doesn't exist: {}", schemaName);
        
        try {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        } catch (Exception e) {
            log.error("Error creating schema: {}", schemaName, e);
            throw new RuntimeException("Failed to create schema: " + schemaName, e);
        }
    }
    
    /**
     * Create table namespaces (prefixes) for the tenant schema
     */
    private void createNamespacesForTenant(String tenantSchema) {
        log.debug("Creating namespaces in schema: {}", tenantSchema);
        
        // List of namespaces to create based on service type
        List<String> namespaces = getNamespacesForService();
        
        for (String namespace : namespaces) {
            try {
                // Create the namespace tables will use
                jdbcTemplate.execute(String.format(
                    "DO $$ BEGIN " +
                    "  IF NOT EXISTS (" +
                    "    SELECT 1 FROM pg_tables " +
                    "    WHERE schemaname = '%s' AND tablename = '%s_namespace'" +
                    "  ) THEN " +
                    "    CREATE TABLE %s.%s_namespace (id SERIAL PRIMARY KEY, description TEXT); " +
                    "    INSERT INTO %s.%s_namespace (description) VALUES ('Namespace for %s tables'); " +
                    "  END IF; " +
                    "END $$;",
                    tenantSchema, namespace, tenantSchema, namespace, tenantSchema, namespace, namespace
                ));
            } catch (Exception e) {
                log.error("Error creating namespace {} in schema {}", namespace, tenantSchema, e);
                throw new RuntimeException("Failed to create namespace", e);
            }
        }
    }
    
    /**
     * Get the list of namespaces needed for this service
     */
    private List<String> getNamespacesForService() {
        List<String> namespaces = new ArrayList<>();
        
        switch (serviceName.toLowerCase()) {
            case "contact-core-service":
                namespaces.add("contact_data");
                break;
            case "contact-activity-service":
                namespaces.add("activity_data");
                break;
            case "authentication-service":
                // Auth service typically doesn't need tenant namespaces
                break;
            default:
                // For other services, use service name as namespace
                namespaces.add(serviceName.toLowerCase().replace("-", "_") + "_data");
        }
        
        return namespaces;
    }
    
    /**
     * Apply Flyway migrations to a schema
     */
    private MigrateResult applyMigrations(String schema, String locations) {
        log.info("Applying migrations to schema {} from locations: {}", schema, locations);
        
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .schemas(schema)
            .locations(locations.split(","))
            .baselineOnMigrate(true)
            .placeholders(Map.of(
                "schema", schema,
                "tablePrefix", tablePrefix != null ? tablePrefix : ""
            ))
            .load();
        
        return flyway.migrate();
    }
    
    /**
     * Register tenant schema in the tenant_schemas table
     */
    private void registerTenantSchema(String tenantId, String schemaName) {
        try {
            // Check if the record already exists
            int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.tenant_schemas WHERE tenant_id = ? AND service_name = ?",
                Integer.class,
                tenantId, serviceName
            );
            
            if (count == 0) {
                // Insert new record
                jdbcTemplate.update(
                    "INSERT INTO public.tenant_schemas (tenant_id, schema_name, service_name, migration_status) VALUES (?, ?, ?, ?)",
                    tenantId, schemaName, serviceName, "COMPLETED"
                );
            } else {
                // Update existing record
                jdbcTemplate.update(
                    "UPDATE public.tenant_schemas SET migration_status = ?, last_validation_at = CURRENT_TIMESTAMP WHERE tenant_id = ? AND service_name = ?",
                    "COMPLETED", tenantId, serviceName
                );
            }
        } catch (Exception e) {
            log.error("Error registering tenant schema", e);
            // Don't fail the whole process if just the registration fails
        }
    }
    
    /**
     * Create a new tenant with the given ID and name
     */
    @Transactional
    public Map<String, Object> createTenant(String tenantId, String name) {
        log.info("Creating new tenant: {} ({})", tenantId, name);
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Insert the tenant record
            jdbcTemplate.update(
                "INSERT INTO public.tenants (tenant_id, name, active) VALUES (?, ?, true)",
                tenantId, name
            );
            
            // Initialize tenant schema
            initializeTenantSchema(tenantId);
            
            // Log the tenant creation
            jdbcTemplate.update(
                "INSERT INTO public.tenant_audit_log (tenant_id, action, performed_by, details) VALUES (?, ?, ?, ?::jsonb)",
                tenantId, "CREATE", "system", "{\"source\": \"" + serviceName + "\"}"
            );
            
            result.put("success", true);
            result.put("tenantId", tenantId);
            result.put("name", name);
            result.put("schemaName", getTenantSchemaName(tenantId));
        } catch (Exception e) {
            log.error("Error creating tenant: {}", tenantId, e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get the service schema name
     */
    public String getServiceSchemaName() {
        return serviceName.toLowerCase().replace("-", "_").replace("service", "");
    }
    
    /**
     * Get the tenant schema name
     */
    public String getTenantSchemaName(String tenantId) {
        return "tenant_" + tenantId.toLowerCase().replace("-", "_");
    }
}