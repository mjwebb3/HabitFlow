package com.habitFlow.Kafka;

/**
 * Enumeration defining the available channels through which a user can receive notifications.
 */
public enum NotificationChannel {

    /** Send notifications via email. */
    EMAIL,

    /** Send notifications via Telegram (TG). */
    TG,

    /** Notifications are disabled for user. */
    NONE
}
