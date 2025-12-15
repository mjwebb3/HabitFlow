package com.habitFlow.habitService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object (DTO) used for dispatching notifications to the Notification Service.
 * This simple DTO encapsulates the necessary information (recipient, subject, and content)
 * required to generate and send a notification (e.g., via email or in- tg message).
 */
@Data
@AllArgsConstructor
public class DispatchNotificationRequest {
    private String username;
    private String subject;
    private String message;
}