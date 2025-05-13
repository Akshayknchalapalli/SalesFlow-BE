package com.salesflow.activity.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;

@TestConfiguration
@EnableDiscoveryClient(autoRegister = false)
@LoadBalancerClients(defaultConfiguration = TestLoadBalancerConfig.class)
public class TestConfig {
}

class TestLoadBalancerConfig {
    @Bean
    public ServiceInstanceListSupplier serviceInstanceListSupplier() {
        return ServiceInstanceListSuppliers.from("contact-core-service");
    }
} 