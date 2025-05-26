package com.salesflow.auth.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Standalone Flyway repair tool that doesn't require Spring context.
 * This can be run directly from the command line.
 * 
 * Usage:
 *   java -cp authentication-service.jar com.salesflow.auth.util.FlywayRepairTool
 *        [config-file] [schema] [migrations-location]
 * 
 * Example:
 *   java -cp authentication-service.jar com.salesflow.auth.util.FlywayRepairTool
 *        src/main/resources/application-dev.yml authentication classpath:db/migration/authentication
 * 
 * If no arguments are provided, it will use default values.
 */
public class FlywayRepairTool {

    public static void main(String[] args) {
        System.out.println("Flyway Repair Tool");
        System.out.println("==================");
        
        try {
            String configFile = args.length > 0 ? args[0] : "application.properties";
            String schema = args.length > 1 ? args[1] : "auth";
            String location = args.length > 2 ? args[2] : "classpath:db/migration/auth";
            
            Properties props = loadProperties(configFile);
            String url = props.getProperty("spring.datasource.url");
            String username = props.getProperty("spring.datasource.username");
            String password = props.getProperty("spring.datasource.password");
            
            if (url == null || username == null || password == null) {
                System.out.println("Could not load database configuration from " + configFile);
                System.out.println("Please provide a valid configuration file or specify connection parameters directly.");
                System.exit(1);
            }
            
            System.out.println("Database URL: " + url);
            System.out.println("Schema: " + schema);
            System.out.println("Migrations location: " + location);
            System.out.println();
            
            repairSchema(url, username, password, schema, location);
            
            System.out.println("Repair completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during repair: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static Properties loadProperties(String path) throws IOException {
        Properties props = new Properties();
        File file = new File(path);
        
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            }
        } else {
            // Try loading from classpath
            try (var is = FlywayRepairTool.class.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    props.load(is);
                }
            }
        }
        
        // Support environment variables
        String dbUrl = System.getenv("DB_URL");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        if (dbUrl != null) props.setProperty("spring.datasource.url", dbUrl);
        if (dbUsername != null) props.setProperty("spring.datasource.username", dbUsername);
        if (dbPassword != null) props.setProperty("spring.datasource.password", dbPassword);
        
        return props;
    }
    
    private static void repairSchema(String url, String username, String password, String schema, String location) {
        System.out.println("Repairing Flyway schema: " + schema);
        
        try {
            // Register JDBC driver
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                System.out.println("PostgreSQL JDBC driver not found. Using default driver.");
            }
            
            // Test connection
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                System.out.println("Database connection successful.");
            }
            
            FluentConfiguration config = Flyway.configure()
                .dataSource(url, username, password)
                .schemas(schema)
                .locations(location)
                .baselineOnMigrate(true)
                .createSchemas(true)
                .outOfOrder(true);
                
            Flyway flyway = config.load();
            flyway.repair();
            
            System.out.println("Successfully repaired schema: " + schema);
        } catch (FlywayException | SQLException e) {
            System.err.println("Error during repair: " + e.getMessage());
            throw new RuntimeException("Failed to repair schema", e);
        }
    }
}