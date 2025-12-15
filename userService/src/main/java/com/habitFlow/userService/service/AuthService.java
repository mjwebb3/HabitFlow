package com.habitFlow.userService.service;

import com.habitFlow.Kafka.ConfirmEmailChannelEvent;
import com.habitFlow.Kafka.CreateInitialNotificationSettingsEvent;
import com.habitFlow.Kafka.SendVerificationEmailEvent;
import com.habitFlow.userService.config.JwtUtil;
import com.habitFlow.userService.dto.*;
import com.habitFlow.userService.exception.custom.*;
import com.habitFlow.userService.model.RefreshToken;
import com.habitFlow.userService.model.User;
import com.habitFlow.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class handling all user authentication-related business logic,
 * including registration, login, token refresh, logout, and email verification.
 * It coordinates with UserRepository, RefreshTokenService, and NotificationProducer.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationProducer notificationProducer;

    @Value("${LinkForVerify}")
    private String linkForVerify;

    /**
     * Registers a new user account.
     * 1. Checks for duplicate username or email.
     * 2. Encodes the password and generates a temporary verification code.
     * 3. Saves the unverified User.
     * 4. Sends events to Kafka to:
     * a) Create initial notification settings.
     * b) Send a verification email.
     *
     * @param request The {@link RegisterRequest} DTO containing username, email, and password.
     * @return A success message.
     * @throws DuplicateUserException If username or email already exists.
     * more info:
     * gives 2 days for user to admit his "gmail.com" if not-> cleanup will delete his data unconfirmed
     * "2day cause of async"
     */
    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new DuplicateUserException("Username already exists");

        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new DuplicateUserException("Email already registered");

        String verificationToken = UUID.randomUUID().toString().substring(0, 6);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .verificationCode(verificationToken)
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        notificationProducer.sendCreateInitialSettings(
                new CreateInitialNotificationSettingsEvent(
                        user.getId(), user.getUsername(), user.getEmail()
                )
        );
        String verificationLink = linkForVerify + user.getEmail() + "&token=" + verificationToken;
        String message = String.format(
                "Hi %s!\n\nClick the link below to verify your email:\n%s",
                user.getUsername(), verificationLink
        );

        notificationProducer.sendVerificationEmail(
                new SendVerificationEmailEvent(
                        user.getEmail(),
                        "HabitFlow Email Verification",
                        message
                )
        );
        return "Registration successful. A confirmation email has been queued for delivery.";
    }

    /**
     * Authenticates a user.
     * 1. Finds the user by username.
     * 2. Checks if the email is verified.
     * 3. Checks if the password matches.
     * 4. Generates a new Access Token and Refresh Token.
     * 5. Updates the user's last active time.
     *
     * @param request The {@link LoginRequest} DTO.
     * @return AuthResponse containing the Access Token and Refresh Token.
     * @throws UserNotFoundException If the user is not found.
     * @throws UnverifiedEmailException If the user's email is not verified.
     * @throws InvalidCredentialsException If the password does not match.
     */
    public AuthResponse login(LoginRequest request) {
        User found = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!found.isEmailVerified())
            throw new UnverifiedEmailException("Please verify your email before logging in");

        if (!passwordEncoder.matches(request.getPassword(), found.getPassword()))
            throw new InvalidCredentialsException("Invalid credentials");

        String accessToken = jwtUtil.generateAccessToken(found.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(found);

        found.setLastActiveAt(LocalDateTime.now());
        userRepository.save(found);
        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    /**
     * Refreshes an expired Access Token using a valid Refresh Token.
     * 1. Finds and validates the Refresh Token (existence, expiration, revocation status).
     * 2. Generates a new Access Token.
     * 3. The original Refresh Token is reused.
     *
     * @param refreshTokenStr The Refresh Token string provided by the client.
     * @return AuthResponse containing the new Access Token and the same Refresh Token.
     * @throws InvalidTokenException If the refresh token is invalid, expired, or revoked.
     */
    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken rt = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!refreshTokenService.validateRefreshToken(rt))
            throw new InvalidTokenException("Refresh token expired");

        String newAccessToken = jwtUtil.generateAccessToken(rt.getUser().getUsername());
        return new AuthResponse(newAccessToken, refreshTokenStr);
    }

    /**
     * Logs out the currently authenticated user by revoking the provided refresh token.
     *
     * @param request LogoutRequest containing the refresh token to revoke
     * @throws InvalidTokenException if the token is missing
     */
    public void logout(TokenRequest request) {

        refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshTokenService.revokeToken(request.getRefreshToken());
    }

    /**
     * Verifies the user's email address using the verification link token.
     * 1. Finds the user by email.
     * 2. Compares the provided token with the stored verification code.
     * 3. Sets emailVerified to true and clears the verification code.
     * 4. Sends a Kafka event to confirm the email channel.
     *
     * @param email The user's email address.
     * @param token The verification code from the link.
     * @return A success message confirming verification.
     * @throws UserNotFoundException If the user is not found.
     * @throws InvalidTokenException If the token is invalid or does not match.
     */
    public String verifyEmail(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!token.equals(user.getVerificationCode()))
            throw new InvalidTokenException("Invalid or expired verification token");

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        notificationProducer.sendConfirmEmailChannel(
                new ConfirmEmailChannelEvent(
                        user.getId(), user.getUsername(), user.getEmail()
                )
        );
        return "Email verified successfully! You can now log in.";
    }
}
