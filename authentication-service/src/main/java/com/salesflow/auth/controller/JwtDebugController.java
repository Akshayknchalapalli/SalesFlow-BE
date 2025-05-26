package com.salesflow.auth.controller;

import com.salesflow.auth.domain.Token;
import com.salesflow.auth.dto.ApiResponseWrapper;
import com.salesflow.auth.dto.AuthResponse;
import com.salesflow.auth.repository.TokenRepository;
import com.salesflow.auth.service.CustomUserDetails;
import com.salesflow.auth.service.CustomUserDetailsService;
import com.salesflow.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/debug/jwt")
@RequiredArgsConstructor
@Tag(name = "JWT Debug", description = "Endpoints for debugging JWT token issues")
public class JwtDebugController {
    private static final Logger logger = LoggerFactory.getLogger(JwtDebugController.class);
    
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final CustomUserDetailsService userDetailsService;
    
    @Operation(summary = "Check token status", description = "Analyzes a JWT token and provides detailed information")
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> analyzeToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> analysis = new HashMap<>();
        
        if (token == null || token.trim().isEmpty()) {
            analysis.put("error", "No token provided");
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error("No token provided: " + analysis));
        }
        
        logger.info("Analyzing token: {}", token);
        
        try {
            // Add basic token info
            analysis.put("token_length", token.length());
            analysis.put("token_format", token.split("\\.").length == 3 ? "Valid JWT format (3 parts)" : "Invalid JWT format");
            
            // Check if it's in the database
            Optional<Token> tokenEntity = tokenRepository.findByRefreshToken(token);
            analysis.put("in_database", tokenEntity.isPresent());
            
            if (tokenEntity.isPresent()) {
                Token refreshToken = tokenEntity.get();
                analysis.put("database_info", Map.of(
                    "id", refreshToken.getId(),
                    "user", refreshToken.getUser().getUsername(),
                    "revoked", refreshToken.isRevoked(),
                    "expiry_date", refreshToken.getExpiryDate().toString(),
                    "expired", refreshToken.getExpiryDate().isBefore(Instant.now())
                ));
            } else {
                analysis.put("database_info", "Token not found in database");
            }
            
            // Analyze JWT claims
            try {
                Claims claims = jwtService.extractAllClaims(token);
                analysis.put("jwt_info", Map.of(
                    "subject", claims.getSubject(),
                    "issued_at", claims.getIssuedAt().toString(),
                    "expiration", claims.getExpiration().toString(),
                    "expired", claims.getExpiration().before(new Date()),
                    "issuer", claims.getIssuer() != null ? claims.getIssuer() : "Not specified",
                    "token_type", claims.containsKey("roles") ? "Access token" : "Refresh token"
                ));
            } catch (Exception e) {
                analysis.put("jwt_parsing_error", e.getMessage());
                logger.error("Error parsing JWT", e);
            }
            
            // Test token validation
            try {
                boolean isValid = jwtService.validateRefreshToken(token);
                analysis.put("validation_result", isValid ? "Valid" : "Invalid");
            } catch (Exception e) {
                analysis.put("validation_error", e.getMessage());
                logger.error("Error validating token", e);
            }
            
            return ResponseEntity.ok(ApiResponseWrapper.success("Token analyzed", analysis));
        } catch (Exception e) {
            analysis.put("error", e.getMessage());
            logger.error("Unexpected error analyzing token", e);
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error("Error analyzing token: " + analysis));
        }
    }
    
    @Operation(summary = "List all refresh tokens", description = "Lists all refresh tokens in the system")
    @GetMapping("/tokens")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> listAllTokens() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var tokens = tokenRepository.findAll();
            result.put("total_count", tokens.size());
            
            var activeTokens = tokens.stream()
                .filter(t -> !t.isRevoked() && t.getExpiryDate().isAfter(Instant.now()))
                .count();
            result.put("active_count", activeTokens);
            
            var tokenList = tokens.stream()
                .map(t -> Map.of(
                    "id", t.getId(),
                    "user", t.getUser().getUsername(),
                    "revoked", t.isRevoked(),
                    "expired", t.getExpiryDate().isBefore(Instant.now()),
                    "expiry_date", t.getExpiryDate().toString(),
                    "token_preview", t.getRefreshToken().substring(0, Math.min(20, t.getRefreshToken().length())) + "..."
                ))
                .toList();
            result.put("tokens", tokenList);
            
            return ResponseEntity.ok(ApiResponseWrapper.success("Tokens retrieved", result));
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error("Error retrieving tokens: "+ result));
        }
    }
    
    @Operation(summary = "Fix all refresh tokens", description = "Updates all refresh tokens to match their JWT expiration")
    @PostMapping("/fix-tokens")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> fixTokens() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var tokens = tokenRepository.findAll();
            result.put("total_tokens", tokens.size());
            
            int fixedCount = 0;
            for (Token token : tokens) {
                try {
                    Claims claims = jwtService.extractAllClaims(token.getRefreshToken());
                    Date expiration = claims.getExpiration();
                    token.setExpiryDate(expiration.toInstant());
                    tokenRepository.save(token);
                    fixedCount++;
                } catch (Exception e) {
                    logger.warn("Could not fix token ID {}: {}", token.getId(), e.getMessage());
                }
            }
            
            result.put("fixed_count", fixedCount);
            return ResponseEntity.ok(ApiResponseWrapper.success("Tokens fixed: " + fixedCount, result));
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error("Error fixing tokens: "+ result));
        }
    }
    
    @Operation(summary = "Create fresh tokens", description = "Creates new access and refresh tokens for a user by username")
    @PostMapping("/create-tokens")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> createFreshTokens(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        Map<String, Object> result = new HashMap<>();
        
        if (username == null || username.trim().isEmpty()) {
            result.put("error", "No username provided");
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error("No username provided: "+ result));
        }
        
        try {
            // Load user details
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
            
            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);
            
            // Create response with new tokens
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .username(userDetails.getUsername())
                    .email(userDetails.getUser().getEmail())
                    .tenantId(userDetails.getTenantId())
                    .roles(userDetails.getAuthorities().stream()
                            .map(authority -> authority.getAuthority())
                            .collect(java.util.stream.Collectors.toSet()))
                    .build();
            
            result.put("auth_response", authResponse);
            result.put("message", "Created fresh tokens for user: " + username);
            return ResponseEntity.ok(ApiResponseWrapper.success("Fresh tokens created", result));
        } catch (Exception e) {
            logger.error("Error creating fresh tokens", e);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error("Error creating tokens: " + e.getMessage() + result));
        }
    }
    
    @Operation(summary = "Direct token refresh", description = "Directly refreshes a token bypassing some validations")
    @PostMapping("/direct-refresh")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> directRefresh(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || token.trim().isEmpty()) {
            result.put("error", "No token provided");
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error("No token provided: "+ result));
        }
        
        try {
            // Extract username from token
            Claims claims = jwtService.extractAllClaims(token);
            String username = claims.getSubject();
            
            if (username == null || username.isEmpty()) {
                result.put("error", "Invalid token - no subject claim");
                return ResponseEntity.badRequest().body(ApiResponseWrapper.error("Invalid token: "+ result));
            }
            
            // Load user details
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
            
            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);
            
            // Create response with new tokens
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .username(userDetails.getUsername())
                    .email(userDetails.getUser().getEmail())
                    .tenantId(userDetails.getTenantId())
                    .roles(userDetails.getAuthorities().stream()
                            .map(authority -> authority.getAuthority())
                            .collect(java.util.stream.Collectors.toSet()))
                    .build();
            
            result.put("auth_response", authResponse);
            return ResponseEntity.ok(ApiResponseWrapper.success("Token refreshed directly", result));
        } catch (Exception e) {
            logger.error("Error in direct refresh", e);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseWrapper.error("Error refreshing token: " + e.getMessage() + result));
        }
    }
}