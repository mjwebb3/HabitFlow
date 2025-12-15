package com.habitFlow.userService.controller;

import com.habitFlow.userService.dto.AuthResponse;
import com.habitFlow.userService.dto.LoginRequest;
import com.habitFlow.userService.dto.TokenRequest;
import com.habitFlow.userService.dto.RegisterRequest;
import com.habitFlow.userService.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller handling all external authentication flows, including
 * user registration, login, token refresh, logout, and email verification.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;

    /**
     * Handles the user registration request.
     * Triggers account creation, initial notification settings, and email verification.
     *
     * @param request The RegisterRequest DTO containing user credentials.
     * @return ResponseEntity with a success message.
     */
    @Operation(summary = "Register new user", description = "Registers a new user and initializes" +
            " notification settings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Parameter(description = "Registration request containing user credentials and email",
                    required = true)
            @Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Handles the user login request.
     * Validates credentials and generates a new pair of Access and Refresh Tokens.
     *
     * @param request The LoginRequest DTO.
     * @return ResponseEntity containing the AuthResponse with tokens.
     */
    @Operation(summary = "User login", description = "Generates access and refresh tokens for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
            @ApiResponse(responseCode = "400", description = "Invalid login data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unverified email"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Login request containing username/email and password",
                    required = true)
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the request to refresh an expired Access Token using a valid Refresh Token.
     *
     * @param request DTO containing the Refresh Token string.
     * @return ResponseEntity containing the new Access Token and the same Refresh Token.
     */
    @Operation(summary = "Refresh access token", description = "Generates new access token using a valid refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access token refreshed"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Parameter(description = "Refresh token string used to generate new access token",
                    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @Valid @RequestBody TokenRequest request
    ) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the user logout process by revoking the provided Refresh Token.
     *
     * @param request DTO containing the Refresh Token to invalidate.
     * @return ResponseEntity with a success message.
     */
    @Operation(summary = "Logout user", description = "Revokes refresh token and logs out the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - token missing or invalid"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> logout(
            @Parameter(description = "Refresh token to invalidate during logout", required = true)
            @Valid @RequestBody TokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Handles the email verification endpoint, usually accessed via a link in the user's email.
     * Validates the token and updates the user's status to verified.
     *
     * @param email The user's email address from the verification link.
     * @param token The verification code from the link.
     * @return ResponseEntity with a success message.
     */
    @Operation(summary = "Verify email", description = "Confirms user email using verification token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired verification token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "User email to verify", example = "user@example.com")
            @RequestParam String email,
            @Parameter(description = "Verification token sent to the user's email", example = "abc123xyz")
            @RequestParam String token) {
        String result = authService.verifyEmail(email, token);
        return ResponseEntity.ok(result);
    }
}
