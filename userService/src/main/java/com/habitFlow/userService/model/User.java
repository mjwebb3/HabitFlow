package com.habitFlow.userService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a user of the system.
 * Stores core authentication data and account status.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user")
public class User{

    /**  Unique identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique username(like login for system). */
    @Column(nullable = false, length = 64, unique = true)
    private String username;

    /** Hashed password. */
    @Column(nullable = false)
    private String password;

    /** Unique email address. */
    @Column(nullable = false,unique = true,length = 320)
    private String email;

    /** Flag indicating if the email address has been verified. */
    @Column(nullable = false)
    private boolean emailVerified = false;

    /** Temporary code used for email verification. */
    private String verificationCode;

    /** Date and time the account was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Date and time of the user's last activity. */
    @Column(nullable = false)
    private LocalDateTime lastActiveAt = LocalDateTime.now();
}
