package com.salesflow.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.salesflow.auth.config.JwtProperties;
import com.salesflow.auth.domain.Token;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.TokenRepository;
import com.salesflow.auth.tenant.TenantContext;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenRepository tokenRepository;
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    private SecretKey getSigningKey() {
        String secretKey = jwtProperties.getSecretKey();
        // Check if the key is long enough for HMAC-SHA256 (at least 256 bits)
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) { // 32 bytes = 256 bits
            // Generate a secure key using Keys.secretKeyFor
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {
        String username = extractUsername(token);
        
        // Extract tenant ID from token and set in context
        Claims claims = extractAllClaims(token);
        if (claims.containsKey("tenantId")) {
            UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
            // Get tenant name if available
            String tenantName = null;
            try {
                if (claims.containsKey("tenantName")) {
                    tenantName = claims.get("tenantName", String.class);
                }
                if (tenantName != null && !tenantName.isEmpty()) {
                    TenantContext.setCurrentTenant(tenantId, tenantName);
                } else {
                    TenantContext.setCurrentTenantId(tenantId);
                }
            } catch (Exception e) {
                TenantContext.setCurrentTenantId(tenantId);
            }
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", userDetails.getTenantId());
        // Add tenant name if available
        if (TenantContext.getCurrentTenantName() != null) {
            claims.put("tenantName", TenantContext.getCurrentTenantName());
        }
        claims.put("roles", userDetails.getAuthorities());
        return generateToken(claims, userDetails.getUser(), jwtProperties.getAccessTokenValidityInMinutes() * 60 * 1000);
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        String refreshToken = generateToken(new HashMap<>(), userDetails.getUser(), 
            jwtProperties.getRefreshTokenValidityInDays() * 24 * 60 * 60 * 1000);
        
        // Revoke any existing tokens for this user
        try {
            tokenRepository.revokeAllUserTokens(userDetails.getUser().getId());
            log.debug("Revoked all existing tokens for user: {}", userDetails.getUsername());
        } catch (Exception e) {
            log.warn("Failed to revoke existing tokens: {}", e.getMessage());
        }
        
        Token token = Token.builder()
                .refreshToken(refreshToken)
                .user(userDetails.getUser())
                .expiryDate(Instant.now().plusSeconds(jwtProperties.getRefreshTokenValidityInDays() * 24 * 60 * 60))
                .revoked(false)
                .build();
        
        tokenRepository.save(token);
        log.debug("Generated new refresh token for user: {}", userDetails.getUsername());
        return refreshToken;
    }

    private String generateToken(Map<String, Object> extraClaims, User user, long validityInMillis) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validityInMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        
        // For tenant-specific users, also validate the tenant
        if (userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            String tokenTenantId = null;
            
            try {
                Claims claims = extractAllClaims(token);
                if (claims.containsKey("tenantId")) {
                    tokenTenantId = claims.get("tenantId", String.class);
                    // Convert both to UUID for comparison
                    UUID tokenTenantUUID = UUID.fromString(tokenTenantId);
                    UUID userTenantUUID = customUserDetails.getTenantId();
                    if (!tokenTenantUUID.equals(userTenantUUID)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                log.warn("Error validating tenant ID in token: {}", e.getMessage());
                return false;
            }
        }
        
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            // First, check if the token format is valid
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                log.warn("Refresh token is null or empty");
                return false;
            }
            
            // Check JWT format validity by trying to extract claims
            try {
                Claims claims = extractAllClaims(refreshToken);
                // Check expiration directly in JWT
                if (claims.getExpiration().before(new Date())) {
                    log.warn("Refresh token JWT is expired");
                    return false;
                }
            } catch (Exception e) {
                log.warn("Invalid JWT format for refresh token: {}", e.getMessage());
                return false;
            }
            
            // Then check if it exists in the database
            Token token = tokenRepository.findByRefreshToken(refreshToken)
                    .orElse(null);
            
            if (token == null) {
                log.warn("Refresh token not found in database");
                return false;
            }
            
            if (token.isRevoked()) {
                log.warn("Refresh token has been revoked");
                return false;
            }
            
            if (token.getExpiryDate().isBefore(Instant.now())) {
                log.warn("Refresh token has expired according to database record");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error validating refresh token", e);
            return false;
        }
    }

    public void revokeRefreshToken(String refreshToken) {
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElse(null);
        
        if (token != null) {
            token.setRevoked(true);
            tokenRepository.save(token);
        }
    }

    public CustomUserDetails getUserDetailsFromToken(String token) {
        try {
            String username = extractUsername(token);
            return (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            log.error("Error getting user details from token", e);
            throw e;
        }
    }
} 