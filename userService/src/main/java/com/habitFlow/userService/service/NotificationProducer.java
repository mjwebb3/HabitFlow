package com.habitFlow.userService.service;

import com.habitFlow.Kafka.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka Producer responsible for sending various notification-related events
 * from the User-Service to the Notification-Service. These events trigger
 * email delivery, initial setup, or channel updates.
 */
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_CREATE_SETTINGS = "notif-create-settings";
    private static final String TOPIC_SEND_EMAIL = "notif-send-verification-email";
    private static final String TOPIC_CONFIRM_EMAIL = "notif-confirm-email-channel";
    private static final String TOPIC_UPDATE_CHANNEL = "notif-update-channel";
    private static final String TOPIC_REGENERATE_TG = "notif-regenerate-tg-token";

    /**
     * Sends an event to trigger the creation of initial notification settings
     * for a newly registered user in the Notification Service.
     *
     * @param event The CreateInitialNotificationSettingsEvent containing user details.
     */
    public void sendCreateInitialSettings(CreateInitialNotificationSettingsEvent event) {
        kafkaTemplate.send(TOPIC_CREATE_SETTINGS, event);
        System.out.println("[NotificationProducer] Sent CreateInitialSettings event " + event);
    }

    /**
     * Sends an event to queue an email verification message for a user.
     *
     * @param event The SendVerificationEmailEvent containing the user's email and verification code.
     */
    public void sendVerificationEmail(SendVerificationEmailEvent event) {
        kafkaTemplate.send(TOPIC_SEND_EMAIL, event);
        System.out.println("[NotificationProducer] Sent VerificationEmail event " + event);
    }

    /**
     * Sends an event to confirm that the user's email address should be set as
     * their primary notification channel.
     *
     * @param event The ConfirmEmailChannelEvent containing the user's ID.
     */
    public void sendConfirmEmailChannel(ConfirmEmailChannelEvent event) {
        kafkaTemplate.send(TOPIC_CONFIRM_EMAIL, event);
        System.out.println("[NotificationProducer] Sent ConfirmEmailChannel event " + event);
    }

    /**
     * Sends an event to update the user's preferred notification channel (e.g., to Telegram or None).
     *
     * @param event The UpdateNotificationChannelEvent containing the user ID and the new channel type.
     */
    public void sendUpdateChannel(UpdateNotificationChannelEvent event) {
        kafkaTemplate.send(TOPIC_UPDATE_CHANNEL, event);
        System.out.println("[NotificationProducer] Sent UpdateNotificationChannel event " + event);
    }

    /**
     * Sends an event to trigger the regeneration of a Telegram token, which is then
     * typically sent to the user via email.
     *
     * @param event The RegenerateTelegramTokenEvent containing the user's ID and contact info.
     */
    public void sendRegenerateTelegramToken(RegenerateTelegramTokenEvent event) {
        kafkaTemplate.send(TOPIC_REGENERATE_TG, event);
        System.out.println("[NotificationProducer] Sent RegenerateTelegramToken event " + event);
    }
}