package com.salesflow.auth.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for cleaning Flyway migration data.
 * Only activated when the property 'spring.flyway.clean-on-start' is set to true.
 * CAUTION: This will delete all data in the configured schemas!
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Run before any other CommandLineRunner
@ConditionalOnProperty(name = "spring.flyway.clean-on-start", havingValue = "true")
public class FlywayCleanupUtil implements CommandLineRunner {

    private final DataSource dataSource;
    
    @Autowired
    public FlywayCleanupUtil(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.warn("CAUTION: Flyway clean-on-start is enabled. This will delete all data in the configured schemas!");
        
        try {
            // Clean authentication schema
            cleanSchema("auth");
            
            // Clean public schema
            cleanSchema("public");
            
            log.info("Flyway cleanup completed successfully");
        } catch (FlywayException e) {
            log.error("Error during Flyway cleanup", e);
            throw e;
        }
    }
    
    private void cleanSchema(String schema) {
        log.info("Cleaning schema: {}", schema);
        
        FluentConfiguration config = Flyway.configure()
            .dataSource(dataSource)
            .schemas(schema)
            .cleanDisabled(false); // Override any clean-disabled setting
            
        Flyway flyway = config.load();
        flyway.clean();
        
        log.info("Successfully cleaned schema: {}", schema);
    }
}