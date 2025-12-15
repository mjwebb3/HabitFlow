package com.habitFlow.notificationService.consumer;

import com.habitFlow.Kafka.*;
import com.habitFlow.notificationService.dto.EmailRequest;
import com.habitFlow.notificationService.dto.NotificationSettingsRequest;
import com.habitFlow.notificationService.service.NotificationConsumer;
import com.habitFlow.notificationService.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

/**
 * Integration tests for the main {@link NotificationConsumer}.
 * This class verifies that the consumer correctly listens to various Kafka topics,
 * handles event data integrity (e.g., expiration), and translates Kafka events
 * into the appropriate method calls on the {@link NotificationService}.
 */
@SpringBootTest
class NotificationConsumerTest {

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private NotificationConsumer consumer;

    @Test
    @DisplayName("✅ ConsumeHabitReminder - Success (Not expired)")
    void testConsumeHabitReminder_NotExpired() {
        HabitReminderEvent event = new HabitReminderEvent(
                "john",
                "Workout",
                "Time to train!",
                LocalDateTime.now()
        );

        consumer.consumeHabitReminder(event);

        verify(notificationService).notifyUser("john", "Habit Reminder", "Time to train!");
    }

    @Test
    @DisplayName("❌ ConsumeHabitReminder - Ignored (Expired event)")
    void testConsumeHabitReminder_Expired() {
        HabitReminderEvent event = new HabitReminderEvent(
                "john",
                "Workout",
                "Time to train!",
                LocalDateTime.now().minusDays(3)
        );

        consumer.consumeHabitReminder(event);

        verify(notificationService, never()).notifyUser(any(), any(), any());
    }

    @Test
    @DisplayName("✅ ConsumeNotification - Success (Generic event)")
    void testConsumeNotification() {
        NotificationEvent event = new NotificationEvent("john", "Hello", "Msg", "habit-service");

        consumer.consumeNotification(event);

        verify(notificationService).notifyUser("john", "Hello", "Msg");
    }

    @Test
    @DisplayName("✅ ConsumeCreateSettings - Initial settings created")
    void testConsumeCreateSettings() {
        CreateInitialNotificationSettingsEvent event =
                new CreateInitialNotificationSettingsEvent(1L, "john", "mail@mail.com");

        consumer.consumeCreateSettings(event);

        verify(notificationService)
                .createInitialSettings(new NotificationSettingsRequest(1L, "john", "mail@mail.com"));
    }

    @Test
    @DisplayName("✅ ConsumeVerificationEmail - Email send requested")
    void testConsumeVerificationEmail() {
        SendVerificationEmailEvent event =
                new SendVerificationEmailEvent("test@mail.com", "Subj", "Msg");

        consumer.consumeSendVerificationEmail(event);

        verify(notificationService)
                .sendEmail(new EmailRequest("test@mail.com", "Subj", "Msg"));
    }

    @Test
    @DisplayName("✅ ConsumeConfirmEmail - Email channel confirmed")
    void testConsumeConfirmEmail() {
        ConfirmEmailChannelEvent event =
                new ConfirmEmailChannelEvent(1L, "username1", "mail@mail.com");

        consumer.consumeConfirmEmail(event);

        verify(notificationService)
                .confirmEmailChannel(1L, "mail@mail.com");
    }

    @Test
    @DisplayName("✅ HandleUpdateChannel - Channel updated to TG")
    void testHandleUpdateChannel() {
        UpdateNotificationChannelEvent event =
                new UpdateNotificationChannelEvent(1L, NotificationChannel.TG);

        consumer.handleUpdateChannel(event);

        verify(notificationService)
                .updateNotificationChannel(1L, NotificationChannel.TG);
    }

    @Test
    @DisplayName("✅ HandleRegenerateTelegram - Telegram token regeneration requested")
    void testRegenerateTelegram() {
        RegenerateTelegramTokenEvent event =
                new RegenerateTelegramTokenEvent(1L, "mail@mail.com", "john");

        consumer.handleRegenerateTelegram(event);

        verify(notificationService)
                .regenerateTelegramToken(1L, "mail@mail.com", "john");
    }
}