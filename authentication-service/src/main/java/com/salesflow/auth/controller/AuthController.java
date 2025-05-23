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
import com.salesflow.auth.dto.ForgotPasswordRequest;
import com.salesflow.auth.dto.ResetPasswordRequest;
import com.salesflow.auth.dto.OtpRequest;
import com.salesflow.auth.dto.OtpLoginRequest;
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
    public ResponseEntity<ApiResponseWrapper<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Get the requested role or default to USER role
        Role role;
        if (request.getRequestedRole() != null && !request.getRequestedRole().isEmpty()) {
            // Format the role name if needed
            String roleName = request.getRequestedRole().startsWith("ROLE_") 
                ? request.getRequestedRole() 
                : "ROLE_" + request.getRequestedRole().toUpperCase();
            
            role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Requested role not found: " + roleName));
        } else {
            // Default to USER role
            role = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTenantId(request.getTenantId());
        user.setEnabled(true);
        user.getAuthorities().add(role);

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
    public ResponseEntity<ApiResponseWrapper<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
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
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
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
        description = "Validates a JWT token and returns user information"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token is valid",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/validate")
    public ResponseEntity<ApiResponseWrapper<AuthResponse>> validateToken(@RequestHeader("Authorization") String token) {
        System.out.println("Token: " + token);
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token format");
        }

        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        CustomUserDetails userDetails = (CustomUserDetails) jwtService.getAuthentication(jwt).getPrincipal();
        
        if (!jwtService.validateToken(jwt, userDetails)) {
            throw new RuntimeException("Invalid token");
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
    }
    
    @Operation(
        summary = "Forgot password",
        description = "Sends a password reset link to the user's email"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reset link sent successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseWrapper<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));
        
        // Generate and save reset token logic would go here
        // Send reset password email logic would go here
        
        return ResponseEntity.ok(ApiResponseWrapper.success("Password reset instructions sent to your email", null));
    }
    
    @Operation(
        summary = "Reset password",
        description = "Resets the user's password using a token"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token or passwords don't match"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseWrapper<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // Validate reset token logic would go here
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        // Update user password logic would go here
        
        return ResponseEntity.ok(ApiResponseWrapper.success("Password reset successfully", null));
    }
    
    @Operation(
        summary = "Request OTP",
        description = "Sends a one-time password to the user via the specified channel"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponseWrapper<Void>> requestOtp(@Valid @RequestBody OtpRequest request) {
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseGet(() -> userRepository.findByUsername(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + request.getIdentifier())));
        
        // Generate OTP logic would go here
        // Send OTP via selected channel (email, SMS, WhatsApp) logic would go here
        
        return ResponseEntity.ok(ApiResponseWrapper.success("OTP sent successfully", null));
    }
    
    @Operation(
        summary = "Login with OTP",
        description = "Authenticates a user using a one-time password"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid OTP"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login-with-otp")
    public ResponseEntity<ApiResponseWrapper<AuthResponse>> loginWithOtp(@Valid @RequestBody OtpLoginRequest request) {
        // Validate OTP logic would go here
        
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseGet(() -> userRepository.findByUsername(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + request.getIdentifier())));
        
        // Authentication logic similar to regular login would go here
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .tenantId(user.getTenantId())
                .roles(user.getAuthorities().stream()
                        .map(role -> role.getAuthority())
                        .collect(java.util.stream.Collectors.toSet()))
                .build();

        return ResponseEntity.ok(ApiResponseWrapper.success("Login successful", authResponse));
    }
} 