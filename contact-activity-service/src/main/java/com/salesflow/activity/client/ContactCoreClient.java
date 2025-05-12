package com.salesflow.activity.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "contact-core-service", path = "/api/v1/contacts")
public interface ContactCoreClient {
    @GetMapping("/{id}")
    ResponseEntity<Object> getContact(@PathVariable("id") UUID id);

    @GetMapping("/{id}/exists")
    ResponseEntity<Boolean> checkContactExists(@PathVariable("id") UUID id);
} 