package com.salesflow.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.salesflow.auth.config.JwtProperties;
import com.salesflow.auth.domain.Token;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.TokenRepository;

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

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
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
        CustomUserDetails customUserDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", userDetails.getTenantId());
        claims.put("roles", userDetails.getAuthorities());
        return generateToken(claims, userDetails.getUser(), jwtProperties.getAccessTokenValidityInMinutes() * 60 * 1000);
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        String refreshToken = generateToken(new HashMap<>(), userDetails.getUser(), 
            jwtProperties.getRefreshTokenValidityInDays() * 24 * 60 * 60 * 1000);
        
        Token token = Token.builder()
                .refreshToken(refreshToken)
                .user(userDetails.getUser())
                .expiryDate(Instant.now().plusSeconds(jwtProperties.getRefreshTokenValidityInDays() * 24 * 60 * 60))
                .revoked(false)
                .build();
        
        tokenRepository.save(token);
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
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean validateRefreshToken(String refreshToken) {
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElse(null);
        
        if (token == null || token.isRevoked() || 
            token.getExpiryDate().isBefore(Instant.now())) {
            return false;
        }
        
        return true;
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
        String username = extractUsername(token);
        return (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);
    }
} 