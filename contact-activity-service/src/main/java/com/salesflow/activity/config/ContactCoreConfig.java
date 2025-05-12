package com.salesflow.activity.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ContactCoreConfig {

    @Value("${contact.core.service.timeout}")
    private int timeout;

    @Value("${contact.core.service.retry.max-attempts}")
    private int maxAttempts;

    @Value("${contact.core.service.retry.initial-interval}")
    private long initialInterval;

    @Value("${contact.core.service.retry.multiplier}")
    private double multiplier;

    @Value("${contact.core.service.retry.max-interval}")
    private long maxInterval;

    @Value("${contact.core.service.circuit-breaker.failure-rate-threshold}")
    private float failureRateThreshold;

    @Value("${contact.core.service.circuit-breaker.wait-duration-in-open-state}")
    private long waitDurationInOpenState;

    @Value("${contact.core.service.circuit-breaker.permitted-number-of-calls-in-half-open-state}")
    private int permittedNumberOfCallsInHalfOpenState;

    @Value("${contact.core.service.circuit-breaker.sliding-window-size}")
    private int slidingWindowSize;

    @Bean
    public RestTemplate contactCoreRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenState))
                .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                .slidingWindowSize(slidingWindowSize)
                .build();
    }

    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofMillis(initialInterval))
                .intervalFunction(interval -> Math.min(
                        (long) (interval * multiplier),
                        maxInterval
                ))
                .build();
    }
} 