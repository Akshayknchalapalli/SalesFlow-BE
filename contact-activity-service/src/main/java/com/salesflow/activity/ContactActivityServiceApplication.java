package com.salesflow.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ContactActivityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContactActivityServiceApplication.class, args);
    }
} 