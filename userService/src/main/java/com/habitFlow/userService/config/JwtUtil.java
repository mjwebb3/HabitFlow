package com.habitFlow.userService.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utility class for handling all JWT operations.
 * This includes generating, validating, and extracting claims from Access Tokens,
 * Refresh Tokens, and specialized Service-to-Service tokens.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String SECRET;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Decodes the base64-encoded secret key and creates the signing key.
     *
     * @return The cryptographic key used for signing and verifying JWTs.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a new Access Token for a given user.
     *
     * @param username The subject (username) for the token.
     * @return The signed JWT string.
     */
    public String generateAccessToken(String username) {
        return buildToken(username, accessTokenExpiration);
    }

    /**
     * Generates a new Refresh Token for a given user.
     *
     * @param username The subject (username) for the token.
     * @return The signed JWT string.
     */
    public String generateRefreshToken(String username) {
        return buildToken(username, refreshTokenExpiration);
    }

    /**
     * Core method to build a JWT with a subject, issued date, expiration, and signature.
     *
     * @param username The token subject.
     * @param expirationMillis The expiration time in milliseconds.
     * @return The compact JWT string.
     */
    private String buildToken(String username, long expirationMillis) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the subject (username) from a token.
     *
     * @param token The JWT string.
     * @return The username.
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Generates a token that is already expired, primarily used for testing purposes.
     *
     * @param username The subject for the expired token.
     * @return The expired JWT string.
     */
    public String generateExpiredToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 10))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates if a token is valid (signed correctly, not expired) and matches the expected username.
     *
     * @param token The JWT string.
     * @param username The expected username.
     * @return true if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token, String username) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(username) && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses the JWT to extract claims (payload).
     *
     * @param token The JWT string.
     * @return The Claims object containing the token payload.
     * @throws JwtException If the token is invalid (bad signature, expired, etc.).
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Generates a specialized token for internal service communication.
     * It sets the subject to the service name and adds a "role": "SERVICE" claim.
     * The expiration is set to 24 hours.
     *
     * @param serviceName The name of the service generating the token (e.g., "user-service").
     * @return The S2S JWT string.
     */
    public String generateServiceToken(String serviceName) {
        return Jwts.builder()
                .setSubject(serviceName)
                .claim("role", "SERVICE")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates if a token is a valid Service-to-Service token and comes from the expected service.
     * It checks for the "role": "SERVICE" claim and subject matching the expected service name
     * (case-insensitive and ignoring hyphens).
     *
     * @param token The JWT string.
     * @param expectedService The name of the service that should have generated the token.
     * @return true if the token is a valid S2S token from the expected source.
     */
    public boolean isServiceToken(String token, String expectedService) {
        try {
            Claims claims = parseClaims(token);
            String role = claims.get("role", String.class);
            String subject = claims.getSubject();
            return "SERVICE".equals(role)
                    && expectedService.replace("-", "")
                    .equalsIgnoreCase(subject.replace("-", ""));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}