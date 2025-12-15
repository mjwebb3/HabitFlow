package com.habitFlow.notificationService.service;

import com.habitFlow.Kafka.*;
import com.habitFlow.notificationService.dto.EmailRequest;
import com.habitFlow.notificationService.dto.NotificationSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Kafka Consumer service responsible for handling various asynchronous notification
 * and configuration events from different microservices.
 * It translates incoming Kafka events into the corresponding business logic calls
 * in the {@link NotificationService}.
 */
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    /**
     * Listens to the 'habit-reminders' topic.
     * Processes habit reminder events, but includes a check to skip reminders
     * that have expired (e.g., if the consumer was down for too long).
     *
     * @param habitEvent The HabitReminderEvent received from Kafka.
     */
    @KafkaListener(topics = "habit-reminders", groupId = "notification-group")
    public void consumeHabitReminder(HabitReminderEvent habitEvent) {
        System.out.printf("[KafkaConsumer] 📥 Habit reminder for '%s'%n", habitEvent.getUsername());

        try {
            if (isExpired(habitEvent.getCreatedAt(), 1)) {
                System.out.printf("[KafkaConsumer] ⏳ Skipped expired reminder for '%s' (created at %s)%n",
                        habitEvent.getUsername(), habitEvent.getCreatedAt());
                return;
            }

            notificationService.notifyUser(
                    habitEvent.getUsername(),
                    "Habit Reminder",
                    habitEvent.getMessage()
            );
        } catch (Exception e) {
            System.err.printf("[KafkaConsumer] ⚠️ Failed to send habit reminder to '%s': %s%n",
                    habitEvent.getUsername(), e.getMessage());
        }
    }

    /**
     * Listens to the 'user-notifications' topic.
     * Processes generic, immediate notification requests (e.g., system alerts, general messages).
     *
     * @param notifEvent The NotificationEvent received from Kafka.
     */
    @KafkaListener(topics = "user-notifications", groupId = "notification-group")
    public void consumeNotification(NotificationEvent notifEvent) {
        System.out.printf("[KafkaConsumer] 📥 Notification for '%s'%n", notifEvent.getUsername());
        try {
            notificationService.notifyUser(
                    notifEvent.getUsername(),
                    notifEvent.getSubject(),
                    notifEvent.getMessage()
            );
        } catch (Exception e) {
            System.err.printf("[KafkaConsumer] ⚠️ Failed to send notification to '%s': %s%n",
                    notifEvent.getUsername(), e.getMessage());
        }
    }

    /**
     * Listens to the 'notif-create-settings' topic.
     * Initiates the creation of default notification settings for a new user.
     *
     * @param event The CreateInitialNotificationSettingsEvent.
     */
    @KafkaListener(topics = "notif-create-settings", groupId = "notification-group")
    public void consumeCreateSettings(CreateInitialNotificationSettingsEvent event) {
        System.out.println("[KafkaConsumer] Create initial settings: " + event);
        notificationService.createInitialSettings(
                new NotificationSettingsRequest(event.getUserId(), event.getUsername(), event.getEmail())
        );
    }

    /**
     * Listens to the 'notif-send-verification-email' topic.
     * Processes requests to send a verification email.
     *
     * @param event The SendVerificationEmailEvent.
     */
    @KafkaListener(topics = "notif-send-verification-email", groupId = "notification-group")
    public void consumeSendVerificationEmail(SendVerificationEmailEvent event) {
        System.out.println("[KafkaConsumer] Send verification email: " + event);
        notificationService.sendEmail(
                new EmailRequest(event.getEmail(), event.getSubject(), event.getMessage())
        );
    }

    /**
     * Listens to the 'notif-confirm-email-channel' topic.
     * Confirms the user's email channel status after successful verification.
     *
     * @param event The ConfirmEmailChannelEvent.
     */
    @KafkaListener(topics = "notif-confirm-email-channel", groupId = "notification-group")
    public void consumeConfirmEmail(ConfirmEmailChannelEvent event) {
        System.out.println("[KafkaConsumer] Confirm email channel: " + event);
        notificationService.confirmEmailChannel(event.getUserId(), event.getEmail());
    }

    /**
     * Listens to the 'notif-update-channel' topic.
     * Updates the user's preferred notification channel (Email/TG/None).
     *
     * @param event The UpdateNotificationChannelEvent.
     */
    @KafkaListener(topics = "notif-update-channel", groupId = "notification-group")
    public void handleUpdateChannel(UpdateNotificationChannelEvent event) {
        notificationService.updateNotificationChannel(event.getUserId(), event.getChannel());
    }

    /**
     * Listens to the 'notif-regenerate-tg-token' topic.
     * Processes the request to generate and send a new Telegram verification token.
     *
     * @param event The RegenerateTelegramTokenEvent.
     */
    @KafkaListener(topics = "notif-regenerate-tg-token", groupId = "notification-group")
    public void handleRegenerateTelegram(RegenerateTelegramTokenEvent event) {

        notificationService.regenerateTelegramToken(
                event.getUserId(),
                event.getEmail(),
                event.getUsername()
        );
    }

    /**
     * Helper method to determine if an event has expired.
     * This prevents sending delayed or irrelevant reminders if the system was processing
     * a backlog of older messages.
     *
     * @param createdAt The timestamp when the event was generated.
     * @param daysToLive The maximum age (in days) the event is considered valid.
     * @return true if the event has expired, false otherwise.
     */
    private boolean isExpired(LocalDateTime createdAt, int daysToLive) {
        if (createdAt == null) return true;
        return createdAt.plusDays(daysToLive).isBefore(LocalDateTime.now());
    }
}
