package com.salesflow.contact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableKafka
@EnableFeignClients(basePackages = {"com.salesflow.auth.client", "com.salesflow.contact.client"})
public class ContactCoreApplication {
    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.load();
        
        // Set the environment variables for Spring Boot
        System.setProperty("SUPABASE_DATABASE_URL", dotenv.get("SUPABASE_DATABASE_URL"));
        System.setProperty("DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
        System.setProperty("DATABASE_USERNAME", dotenv.get("DATABASE_USERNAME"));
        SpringApplication.run(ContactCoreApplication.class, args);
    }
} 