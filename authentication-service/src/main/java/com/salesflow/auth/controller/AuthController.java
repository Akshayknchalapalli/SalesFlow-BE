package com.salesflow.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.salesflow.auth.domain.Role;
import com.salesflow.auth.domain.User;
import com.salesflow.auth.dto.AuthRequest;
import com.salesflow.auth.dto.AuthResponse;
import com.salesflow.auth.dto.RegisterRequest;
import com.salesflow.auth.dto.ApiResponseWrapper;
import com.salesflow.auth.repository.RoleRepository;
import com.salesflow.auth.repository.UserRepository;
import com.salesflow.auth.service.CustomUserDetails;
import com.salesflow.auth.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided details and returns authentication tokens"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseWrapper<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader("X-Tenant-ID") String tenantId) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenantId(tenantId);
        user.setEnabled(true);
        user.getAuthorities().add(userRole);

        userRepository.save(user);

        AuthResponse authResponse = AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .tenantId(user.getTenantId())
                .roles(user.getAuthorities().stream()
                        .map(role -> role.getAuthority())
                        .collect(java.util.stream.Collectors.toSet()))
                .build();

        return ResponseEntity.ok(ApiResponseWrapper.success("User registered successfully", authResponse));
    }

    @Operation(
        summary = "Login user",
        description = "Authenticates user credentials and returns authentication tokens"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseWrapper<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request,
            @RequestHeader("X-Tenant-ID") String tenantId) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // Verify tenant ID matches
        if (!userDetails.getTenantId().equals(tenantId)) {
            return ResponseEntity.status(401)
                .body(ApiResponseWrapper.error("Invalid tenant ID"));
        }

        String accessToken = jwtService.generateAccessToken(userDetails.getUser());
        String refreshToken = jwtService.generateRefreshToken(userDetails.getUser());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(userDetails.getUsername())
                .email(userDetails.getUser().getEmail())
                .tenantId(userDetails.getTenantId())
                .roles(userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(java.util.stream.Collectors.toSet()))
                .build();

        return ResponseEntity.ok(ApiResponseWrapper.success("Login successful", authResponse));
    }

    @Operation(
        summary = "Refresh authentication token",
        description = "Generates new access and refresh tokens using a valid refresh token"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseWrapper<AuthResponse>> refreshToken(@Valid @RequestBody String refreshToken) {
        // Remove any quotes from the refresh token if present
        refreshToken = refreshToken.replaceAll("^\"|\"$", "");
        
        if (!jwtService.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(401)
                .body(ApiResponseWrapper.error("Invalid refresh token"));
        }

        String username = jwtService.extractUsername(refreshToken);
        CustomUserDetails userDetails = (CustomUserDetails) jwtService.getAuthentication(refreshToken).getPrincipal();
        
        String newAccessToken = jwtService.generateAccessToken(userDetails.getUser());
        String newRefreshToken = jwtService.generateRefreshToken(userDetails.getUser());

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

        return ResponseEntity.ok(ApiResponseWrapper.success("Token refreshed successfully", authResponse));
    }

    @Operation(
        summary = "Validate token",
        description = "Validates a JWT token and returns user information",
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token is valid",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/validate")
    public ResponseEntity<ApiResponseWrapper<AuthResponse>> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                .body(ApiResponseWrapper.error("Invalid token format. Token must start with 'Bearer '"));
        }

        String jwt = authHeader.substring(7).trim();
        try {
            String username = jwtService.extractUsername(jwt);
            CustomUserDetails userDetails = (CustomUserDetails) jwtService.getAuthentication(jwt).getPrincipal();
            
            if (!jwtService.validateToken(jwt, userDetails)) {
                return ResponseEntity.status(401)
                    .body(ApiResponseWrapper.error("Invalid or expired token"));
            }

            AuthResponse authResponse = AuthResponse.builder()
                    .username(userDetails.getUsername())
                    .email(userDetails.getUser().getEmail())
                    .tenantId(userDetails.getTenantId())
                    .roles(userDetails.getAuthorities().stream()
                            .map(authority -> authority.getAuthority())
                            .collect(java.util.stream.Collectors.toSet()))
                    .build();

            return ResponseEntity.ok(ApiResponseWrapper.success("Token is valid", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(ApiResponseWrapper.error("Invalid token: " + e.getMessage()));
        }
    }
} 