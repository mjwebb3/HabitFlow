package com.habitFlow.notificationService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used for several purposes related to user settings:
 * 1. Initial creation of settings (via Kafka or REST).
 * 2. Confirmation of the email channel.
 * 3. Regeneration of the Telegram token (requires userId, email, and username).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for creating or updating notification settings")
public class NotificationSettingsRequest {

    @Schema(description = "Unique user ID", example = "42")
    @NotNull(message = "User ID is required")
    private Long userId;

    @Schema(description = "User email address", example = "user@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Username of the account", example = "john_doe")
    private String username;
}
