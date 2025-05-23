package com.salesflow.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.salesflow.auth.config.JwtProperties;
import com.salesflow.auth.domain.Token;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.repository.TokenRepository;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private CustomUserDetails testUserDetails;
    private String testSecretKey = "testSecretKey123456789012345678901234567890123456789012345678901234567890";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setTenantId("tenant1");
        testUser.setEnabled(true);

        testUserDetails = new CustomUserDetails(testUser);

        lenient().when(jwtProperties.getSecretKey()).thenReturn(testSecretKey);
        lenient().when(jwtProperties.getAccessTokenValidityInMinutes()).thenReturn(30L);
        lenient().when(jwtProperties.getRefreshTokenValidityInDays()).thenReturn(7L);
    }

    @Test
    void generateAccessToken_ShouldCreateValidToken() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token, testUser));
    }

    @Test
    void generateRefreshToken_ShouldCreateValidToken() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertNotNull(refreshToken);
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isValid = jwtService.validateToken(token, testUser);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateRefreshToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String refreshToken = jwtService.generateRefreshToken(testUser);
        Token token = new Token();
        token.setRefreshToken(refreshToken);
        token.setRevoked(false);
        token.setExpiryDate(Instant.now().plusSeconds(3600)); // Set expiry date 1 hour from now
        when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(token));

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
        when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(token));

        // When
        jwtService.revokeRefreshToken(refreshToken);

        // Then
        verify(tokenRepository).save(argThat(t -> t.isRevoked()));
    }

    @Test
    void getUserDetailsFromToken_ShouldReturnUserDetails() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(testUserDetails);

        // When
        CustomUserDetails userDetails = jwtService.getUserDetailsFromToken(token);

        // Then
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), userDetails.getUsername());
    }
} 