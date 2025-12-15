package com.habitFlow.notificationService.model;

/**
 * Enumeration defining the possible states of a user's notification channel.
 */
public enum NotificationStatus {

    /**
     * The channel setting is awaiting confirmation (e.g., waiting for user to click
     * an email link or verify a Telegram token).
     */
    PENDING,

    /**
     * The channel is confirmed and ready to be used for sending notifications.
     */
    CONFIRMED,

    /**
     * A persistent error occurred when attempting to use this channel
     * (e.g., repeated email delivery failures). The channel should not be used until resolved.
     */
    FAILED,

    /**
     * The channel is actively disabled by the user, regardless of its confirmation status.
     */
    DISABLED
}
