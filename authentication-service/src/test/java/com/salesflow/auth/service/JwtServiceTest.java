package com.salesflow.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;

import com.salesflow.auth.config.JwtProperties;
import com.salesflow.auth.domain.Token;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.TokenRepository;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @Mock(lenient = true)

    private JwtProperties jwtProperties;

    @Mock(lenient = true)
    private CustomUserDetailsService userDetailsService;

    @Mock(lenient = true)
    private TokenRepository tokenRepository;

    private JwtService jwtService;

    private User testUser;
    private CustomUserDetails testUserDetails;
    private String testSecretKey = "testSecretKey123456789012345678901234567890123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setTenantId(UUID.randomUUID());
        testUser.setEnabled(true);

        testUserDetails = new CustomUserDetails(testUser);

        lenient().when(jwtProperties.getSecretKey()).thenReturn(testSecretKey);
        lenient().when(jwtProperties.getAccessTokenValidityInMinutes()).thenReturn(30L);
        lenient().when(jwtProperties.getRefreshTokenValidityInDays()).thenReturn(7L);
        
        jwtService = new JwtService(jwtProperties, userDetailsService, tokenRepository);
    }

    @Test
    void generateAccessToken_ShouldCreateValidToken() {
        // Set up the mock before generating the token
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(testUserDetails);

        // When
        String token = jwtService.generateAccessToken(testUserDetails);

        // Then
        assertNotNull(token);
        // Extract claims to verify tenant ID
        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(testUser.getTenantId().toString(), claims.get("tenantId", String.class));
        assertTrue(jwtService.validateToken(token, testUserDetails));
    }

    @Test
    void generateRefreshToken_ShouldCreateValidToken() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testUserDetails);

        // Then
        assertNotNull(refreshToken);
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Set up the mock before generating the token
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(testUserDetails);

        // Given
        String token = jwtService.generateAccessToken(testUserDetails);

        // When
        boolean isValid = jwtService.validateToken(token, testUserDetails);

        // Then
        assertTrue(isValid);
        // Verify tenant ID in claims
        Claims claims = jwtService.extractAllClaims(token);
        assertEquals(testUser.getTenantId().toString(), claims.get("tenantId", String.class));
    }

    @Test
    void validateRefreshToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String refreshToken = jwtService.generateRefreshToken(testUserDetails);
        Token token = new Token();
        token.setRefreshToken(refreshToken);
        token.setRevoked(false);
        token.setExpiryDate(Instant.now().plusSeconds(3600)); // Set expiry date 1 hour from now
        lenient().when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(token));

        // When
        boolean isValid = jwtService.validateRefreshToken(refreshToken);

        // Then
        assertTrue(isValid);
    }

    @Test
    void revokeRefreshToken_ShouldRevokeToken() {
        // Given
        String refreshToken = "test-refresh-token";
        Token token = new Token();
        token.setRefreshToken(refreshToken);
        token.setRevoked(false);
        lenient().when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(token));

        // When
        jwtService.revokeRefreshToken(refreshToken);

        // Then
        verify(tokenRepository).save(argThat(t -> t.isRevoked()));
    }

    @Test
    void getUserDetailsFromToken_ShouldReturnUserDetails() {
        // Given
        String token = jwtService.generateAccessToken(testUserDetails);
        lenient().when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(testUserDetails);

        // When
        CustomUserDetails userDetails = jwtService.getUserDetailsFromToken(token);

        // Then
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), userDetails.getUsername());
    }

    @Test
    void generateToken_ShouldGenerateValidToken() {
        // Arrange
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email,
                "password",
                Collections.emptyList()
        );
        // ... existing code ...
    }
} 