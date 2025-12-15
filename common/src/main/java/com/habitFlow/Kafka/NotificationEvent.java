package com.habitFlow.Kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * General Kafka event used by any microservice to request the Notification Service
 * to send an immediate notification to a user.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {

    /** The username of the recipient (resolved to contact info by the Notification Service). */
    private String username;

    /** The subject or title of the notification. */
    private String subject;

    /** The main content or body of the message. */
    private String message;

    /** The microservice that produced this event ("habit-service" or "user-service"). */
    private String source;
}