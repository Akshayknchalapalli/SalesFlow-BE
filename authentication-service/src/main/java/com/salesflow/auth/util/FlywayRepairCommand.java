package com.salesflow.auth.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import com.salesflow.auth.AuthServiceApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * Standalone command-line utility for repairing Flyway migrations.
 * 
 * This utility helps fix issues with Flyway migration checksums, missing migrations,
 * and other validation errors.
 * 
 * Usage:
 * 1. Run with the 'repair' argument to repair all schemas
 * 2. Run with the 'repair-auth' argument to repair only the authentication schema
 * 3. Run with the 'repair-public' argument to repair only the public schema
 * 
 * Example:
 * java -cp "your-app.jar" com.salesflow.auth.util.FlywayRepairCommand repair
 */
@Slf4j
@SpringBootApplication
@ComponentScan(
    basePackages = "com.salesflow.auth",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = AuthServiceApplication.class
    )
)
public class FlywayRepairCommand implements CommandLineRunner {

    private final DataSource dataSource;
    private final ApplicationContext context;
    
    @Autowired
    public FlywayRepairCommand(DataSource dataSource, ApplicationContext context) {
        this.dataSource = dataSource;
        this.context = context;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(FlywayRepairCommand.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Find our command in args, ignoring Spring Boot arguments (which start with --)
        String command = null;
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                command = arg.toLowerCase();
                break;
            }
        }
        
        if (command == null) {
            log.info("No command specified. Available commands: repair, repair-auth, repair-public");
            return; // Don't exit, let Spring Boot handle shutdown
        }
        
        try {
            switch (command) {
                case "repair":
                    repairAllSchemas();
                    break;
                case "repair-auth":
                    repairSchema("auth", "classpath:db/migration/auth");
                    break;
                case "repair-public":
                    repairSchema("public", "classpath:db/migration/public");
                    break;
                default:
                    log.info("Unknown command: {}. Available commands: repair, repair-auth, repair-public", command);
                    break;
            }
        } catch (Exception e) {
            log.error("Error during Flyway repair", e);
        }
        
        // Let Spring handle the exit
    }
    
    private void repairAllSchemas() {
        log.info("Repairing all Flyway schemas");
        
        try {
            // Repair authentication schema
            repairSchema("auth", "classpath:db/migration/auth");
            
            // Repair public schema
            repairSchema("public", "classpath:db/migration/public");
            
            log.info("Flyway repair completed successfully for all schemas");
        } catch (FlywayException e) {
            log.error("Error during Flyway repair", e);
            throw e;
        }
    }
    
    private void repairSchema(String schema, String location) {
        log.info("Repairing Flyway schema: {}", schema);
        
        FluentConfiguration config = Flyway.configure()
            .dataSource(dataSource)
            .schemas(schema)
            .locations(location)
            .baselineOnMigrate(true)
            .createSchemas(true)
            .outOfOrder(true);
            
        Flyway flyway = config.load();
        flyway.repair();
        
        log.info("Successfully repaired schema: {}", schema);
    }
}