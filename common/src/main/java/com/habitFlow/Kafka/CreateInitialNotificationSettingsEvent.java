package com.habitFlow.Kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka event dispatched by the User Service upon successful registration
 * to instruct the Notification Service to create the initial notification
 * settings record for the new user (defaulting to EMAIL channel).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInitialNotificationSettingsEvent {

    /** The ID of the newly created user. */
    private Long userId;

    /** The username of the new user. */
    private String username;

    /** The email address of the new user. */
    private String email;
}