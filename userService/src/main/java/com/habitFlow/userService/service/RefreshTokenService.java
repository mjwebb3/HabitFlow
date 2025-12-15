package com.habitFlow.userService.service;

import com.habitFlow.userService.model.RefreshToken;
import com.habitFlow.userService.model.User;
import com.habitFlow.userService.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that manages the lifecycle and validation of Refresh Tokens.
 * Responsible for creating, searching for, checking the expiration date/status, and revoking tokens.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Creates and saves a new Refresh Token for the specified user.
     * The token has a unique UUID and sets an expiration date based on the configuration.
     *
     * @param user The user for whom the token is created.
     * @return The saved {@link RefreshToken} object.
     */
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Searches for a Refresh Token by its string value (UUID).
     *
     * @param token String representation of the token.
     * @return Optional containing {@link RefreshToken}, if found.
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Checks whether the Refresh Token is valid:
     * whether it has been revoked or has expired.
     *
     * @param token RefreshToken object for validation.
     * @return true if the token is valid and has not expired; false otherwise.
     */
    public boolean validateRefreshToken(RefreshToken token) {
        return token != null && !token.isRevoked()
                && token.getExpiryDate().isAfter(Instant.now());
    }

    /**
     * Revokes (cancels) the Refresh Token by setting the `revoked` flag to true.
     * Used when logging out of the system.
     *
     * @param token String representation of the token to be revoked.
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}