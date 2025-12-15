package com.habitFlow.Kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka event used to request the Notification Service to dispatch a verification
 * email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendVerificationEmailEvent {

    /** The recipient's email address. */
    private String email;

    /** The subject line of the email. */
    private String subject;

    /** The content or body of the verification message. */
    private String message;
}