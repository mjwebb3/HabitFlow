package com.habitFlow.notificationService.service;

import com.habitFlow.notificationService.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * NotificationFacade centralizes business logic for notification-related operations.
 * It acts as an intermediary layer between the REST Controller and the core business logic
 * in {@link NotificationService}. Its primary roles are to delegate requests, perform
 * minor data transformations (e.g., setting default values), and return standardized
 * HTTP responses ({@link ResponseEntity}).
 */
@Service
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;

    /**
     * Delegates the request to send a plain email notification.
     *
     * @param request The EmailRequest DTO.
     * @return ResponseEntity with status 200 OK and a success message.
     */
    public ResponseEntity<String> sendEmail(EmailRequest request) {
        notificationService.sendEmail(request);
        return ResponseEntity.ok("Email sent successfully!");
    }

    /**
     * Delegates the request to create initial notification settings for a new user.
     *
     * @param request The NotificationSettingsRequest DTO.
     * @return ResponseEntity with status 200 OK.
     */
    public ResponseEntity<Void> createSettings(NotificationSettingsRequest request) {
        notificationService.createInitialSettings(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Delegates the request to update the user's preferred notification channel.
     *
     * @param request The UpdateChannelRequest DTO containing userId and new channel.
     * @return ResponseEntity with status 200 OK.
     */
    public ResponseEntity<Void> updateChannel(UpdateChannelRequest request) {
        notificationService.updateNotificationChannel(
                request.getUserId(),
                request.getChannel()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * Delegates the request to regenerate a time-limited Telegram verification token.
     *
     * @param request The NotificationSettingsRequest DTO containing user info.
     * @return ResponseEntity with status 200 OK and a confirmation message.
     */
    public ResponseEntity<String> regenerateToken(NotificationSettingsRequest request) {
        notificationService.regenerateTelegramToken(
                request.getUserId(), request.getEmail(), request.getUsername()
        );
        return ResponseEntity.ok("Telegram token regenerated successfully");
    }

    /**
     * Dispatches a notification to a specific user, using the user's current settings
     * (Email or Telegram).
     * If the subject is null in the request, a default subject is used.
     *
     * @param request The DispatchNotificationRequest DTO.
     * @return ResponseEntity with status 200 OK.
     */
    public ResponseEntity<Void> dispatchNotification(DispatchNotificationRequest request) {
        String subject = request.getSubject() != null ? request.getSubject() : "HabitFlow Notification";
        notificationService.notifyUser(request.getUsername(), subject, request.getMessage());
        return ResponseEntity.ok().build();
    }

    /**
     * Delegates the request to confirm the user's email notification channel.
     *
     * @param request The NotificationSettingsRequest DTO containing user ID and email.
     * @return ResponseEntity with status 200 OK.
     */
    public ResponseEntity<Void> confirmEmail(NotificationSettingsRequest request) {
        notificationService.confirmEmailChannel(request.getUserId(), request.getEmail());
        return ResponseEntity.ok().build();
    }
}
