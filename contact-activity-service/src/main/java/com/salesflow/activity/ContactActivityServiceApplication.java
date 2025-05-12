package com.salesflow.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import com.salesflow.activity.config.LoadBalancerConfiguration;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@LoadBalancerClients(defaultConfiguration = LoadBalancerConfiguration.class)
public class ContactActivityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContactActivityServiceApplication.class, args);
    }
} 