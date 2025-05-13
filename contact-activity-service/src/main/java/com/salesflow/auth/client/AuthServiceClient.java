package com.salesflow.auth.client;

import com.salesflow.auth.dto.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthServiceClient {
    @GetMapping("/validate")
    ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String token);
} 