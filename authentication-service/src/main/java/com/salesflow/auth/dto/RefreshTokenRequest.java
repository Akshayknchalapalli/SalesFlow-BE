package com.salesflow.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for refresh token requests.
 * This class represents the expected request body when refreshing an authentication token.
 * The refreshToken field must contain a valid JWT refresh token that was previously issued.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for token refresh operations")
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token is required")
    @Pattern(regexp = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$", 
             message = "Invalid JWT token format")
    @Schema(
        description = "The refresh token that was provided during login or previous refresh",
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYxMjM0NTY3OCwiZXhwIjoxNjEyOTUwNDc4fQ.kD4kYlX8U8xJ4HmScOL_3Ss1vjuTRFoNX1sV8HcO-8A",
        required = true
    )
    private String refreshToken;
}