package com.salesflow.contact.client;

import com.salesflow.contact.dto.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authentication-service", url = "${AUTH_SERVICE_URL:http://localhost:8081}")
public interface AuthServiceClient {
    @PostMapping("/api/auth/validate")
    AuthResponse validateToken(@RequestHeader("Authorization") String token);
} 