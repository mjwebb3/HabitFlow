package com.habitFlow.Kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka event used by the User Service or Notification Settings API
 * to notify the Notification Service about a change in a user's preferred
 * {@link NotificationChannel}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNotificationChannelEvent {

    /** The ID of the user whose channel is being updated. */
    private Long userId;

    /** The new preferred channel setting (EMAIL, TG, or NONE). */
    private NotificationChannel channel;
}
