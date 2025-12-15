package com.habitFlow.Kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka event dispatched internally (after user clicks an email link)
 * to confirm and activate the user's email notification channel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmEmailChannelEvent {

    /** The ID of the user whose email channel is being confirmed. */
    private Long userId;

    /** The username of the user. */
    private String username;

    /** The email address of the user. */
    private String email;
}