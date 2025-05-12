package com.salesflow.activity.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@LoadBalancerClient(name = "contact-core-service", configuration = LoadBalancerConfiguration.class)
public class LoadBalancerConfiguration {

    @Bean
    @LoadBalanced
    public ServiceInstanceListSupplier serviceInstanceListSupplier(Environment environment) {
        return ServiceInstanceListSuppliers.from("contact-core-service");
    }
} 