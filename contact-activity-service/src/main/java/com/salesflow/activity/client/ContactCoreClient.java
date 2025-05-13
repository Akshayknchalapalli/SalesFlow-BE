package com.salesflow.activity.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.UUID;

@FeignClient(name = "contact-core-service", path = "/api/v1/contacts")
public interface ContactCoreClient {
    @GetMapping("/{id}")
    @CircuitBreaker(name = "contactCoreService")
    @Retry(name = "contactCoreService")
    ResponseEntity<Object> getContact(@PathVariable("id") UUID id);

    @GetMapping("/{id}/exists")
    @CircuitBreaker(name = "contactCoreService")
    @Retry(name = "contactCoreService")
    ResponseEntity<Boolean> checkContactExists(@PathVariable("id") UUID id);
} 