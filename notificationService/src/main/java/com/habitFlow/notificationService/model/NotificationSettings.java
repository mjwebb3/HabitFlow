package com.habitFlow.notificationService.model;

import com.habitFlow.Kafka.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;
import java.time.LocalDateTime;

/**
 * Entity storing user notification channel settings.
 * It determines how a user receives reminders (Email, Telegram, or None).
 */
@Entity
@Table(name = "notification_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettings {

    /** The unique identifier for the notification settings record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The ID of the user whose settings these are. */
    private Long userId;

    /** The selected notification channel (Default: EMAIL). */
    @Default
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel = NotificationChannel.EMAIL;

    /** The address for notifications: email, Telegram Chat ID, or a temporary token. */
    private String address;

    /** Whether notifications are enabled (Default: true). */
    @Default
    private boolean enabled = true;

    /** The current status of the channel (PENDING, CONFIRMED, FAILED, DISABLED). */
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    /** Timestamp of record creation. */
    @Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Timestamp of the last update. */
    @Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** Expiration time for temporary verification tokens (used for TG). */
    private LocalDateTime expiryAt;

    /**
     * Automatically updates the {@code updatedAt} timestamp before every persistence update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
