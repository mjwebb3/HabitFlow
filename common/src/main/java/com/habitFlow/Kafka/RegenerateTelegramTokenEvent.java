package com.habitFlow.Kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka event used to trigger the Notification Service to create a new
 * temporary verification token for linking a user's account to Telegram.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegenerateTelegramTokenEvent {

    /** The ID of the user requesting the new token. */
    private Long userId;

    /** The user's associated email. */
    private String email;

    /** The user's username. */
    private String username;
}
