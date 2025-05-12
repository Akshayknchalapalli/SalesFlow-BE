package com.salesflow.contact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import io.github.cdimascio.dotenv.Dotenv;
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableKafka
public class ContactCoreApplication {
    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.load();
        
        // Set the environment variables for Spring Boot
        System.setProperty("SUPABASE_DB_URL", dotenv.get("SUPABASE_DB_URL"));
        System.setProperty("SUPABASE_DB_PASSWORD", dotenv.get("SUPABASE_DB_PASSWORD"));
        SpringApplication.run(ContactCoreApplication.class, args);
    }
} 