package com.salesflow.contact.controller;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
public class HealthController implements HealthIndicator {
    private final MeterRegistry meterRegistry;

    @Override
    public Health health() {
        return Health.up()
                .withDetail("service", "contact-core-service")
                .build();
    }

    @GetMapping("/metrics")
    public String metrics() {
        return meterRegistry.getMeters().toString();
    }
} 