package com.habitFlow.userService.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Entity representing a Refresh Token, used for securely obtaining new Access Tokens.
 * It is linked to the {@link User} entity via a Many-to-One relationship.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    /** Unique identifier for the token record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The unique string value (UUID) of the refresh token. */
    private String token;

    /** The exact time (UTC) when the token expires. */
    private Instant expiryDate;

    /** Flag to manually invalidate the token (upon logout). */
    private boolean revoked;

    /**
     * Ensures tokens are deleted automatically if the associated user is deleted.
     * Reference to the User who owns this token.
     * */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}