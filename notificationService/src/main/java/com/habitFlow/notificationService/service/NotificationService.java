package com.habitFlow.notificationService.service;

import com.habitFlow.Kafka.NotificationChannel;
import com.habitFlow.notificationService.config.UserService;
import com.habitFlow.notificationService.dto.EmailRequest;
import com.habitFlow.notificationService.dto.NotificationSettingsRequest;
import com.habitFlow.notificationService.dto.UserDto;
import com.habitFlow.notificationService.exception.custom.ForbiddenActionException;
import com.habitFlow.notificationService.exception.custom.NotificationNotFoundException;
import com.habitFlow.notificationService.exception.custom.NotificationSendException;
import com.habitFlow.notificationService.model.NotificationSettings;
import com.habitFlow.notificationService.model.NotificationStatus;
import com.habitFlow.notificationService.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Core service layer for handling all notification-related business logic.
 * This includes email sending, managing user notification settings (channels, status),
 * handling Telegram token generation, and dispatching notifications based on user preference.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository settingsRepo;
    private final TelegramBotService telegramBotService;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String username;

    /**
     * Sends an email using JavaMailSender.
     * If the email sending fails, it updates the notification status to FAILED
     * for the corresponding recipient (if found) and throws a NotificationSendException.
     *
     * @param request The EmailRequest containing recipient, subject, and message.
     * @throws NotificationSendException if the email sending fails.
     */
    public void sendEmail(EmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setTo(request.getTo());
            helper.setFrom(username);
            helper.setSubject(request.getSubject());
            helper.setText(request.getMessage(), false);

            mailSender.send(message);
            System.out.println("[NotificationService] Email sent to " + request.getTo());
        } catch (Exception e) {

            NotificationSettings settings = settingsRepo.findByAddress(request.getTo()).orElse(null);
            if (settings != null) {
                settings.setStatus(NotificationStatus.FAILED);
                settings.setUpdatedAt(LocalDateTime.now());
                settingsRepo.save(settings);
            }
            throw new NotificationSendException("Failed to send email to " + request.getTo());
        }

    }

    /**
     * Creates the initial notification settings for a new user, typically after registration.
     * The initial channel is set to EMAIL with PENDING status.
     *
     * @param request The initial settings request (from User Service).
     */
   public void createInitialSettings(NotificationSettingsRequest request) {
       NotificationSettings settings = NotificationSettings.builder()
               .userId(request.getUserId())
               .channel(NotificationChannel.EMAIL)
               .address(request.getEmail())
               .enabled(true)
               .status(NotificationStatus.PENDING)
               .createdAt(LocalDateTime.now())
               .updatedAt(LocalDateTime.now())
               .build();

       settingsRepo.save(settings);
   }

    /**
     * Updates the user's primary notification channel (Email, Telegram, or None).
     * Handles specific channel transitions (e.g., clearing address and setting PENDING status for TG).
     *
     * @param userId The ID of the user to update.
     * @param newChannel The newly requested notification channel.
     * @throws NotificationNotFoundException if settings for the user are not found.
     */
    public void updateNotificationChannel(Long userId, NotificationChannel newChannel) {
        NotificationSettings settings = settingsRepo.findByUserIdAndEnabled(userId, true)
                .orElseThrow(() -> new NotificationNotFoundException("Notification settings not found"));

        if (settings.getChannel() != newChannel) {
            settings.setChannel(newChannel);

            switch (newChannel) {
                case TG -> {
                    settings.setAddress(null); // The address will be the temporary token
                    settings.setStatus(NotificationStatus.PENDING);
                }
                case EMAIL -> {
                    // Assuming the email address is already stored/known
                    settings.setAddress(settings.getAddress());
                    settings.setStatus(NotificationStatus.CONFIRMED);
                }
                case NONE -> {
                    settings.setAddress(null);
                    settings.setStatus(NotificationStatus.DISABLED);
                }
            }
            settings.setUpdatedAt(LocalDateTime.now());
            settingsRepo.save(settings);
        }
    }

    /**
     * Generates a unique, time-limited token for Telegram verification and sends it via email.
     *
     * @param userId The ID of the user.
     * @param email The user's email address (for sending the token).
     * @param username The user's username (for email personalization).
     * @throws NotificationNotFoundException if settings are not found.
     * @throws ForbiddenActionException if the user attempts to regenerate a TG token when TG is not the
     * selected channel.
     */
    public void regenerateTelegramToken(Long userId, String email, String username) {
        NotificationSettings settings = settingsRepo.findByUserIdAndEnabled(userId, true)
                .orElseThrow(() -> new NotificationNotFoundException("Notification settings not found"));

        if (settings.getChannel() != NotificationChannel.TG) {
            settings.setStatus(NotificationStatus.FAILED);
            settingsRepo.save(settings);
            throw new ForbiddenActionException("Telegram channel is not selected");
        }

        //token generation and set expiration
        String tgToken = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        settings.setAddress(tgToken);
        settings.setStatus(NotificationStatus.PENDING);
        settings.setExpiryAt(LocalDateTime.now().plusHours(24));
        settings.setUpdatedAt(LocalDateTime.now());
        settingsRepo.save(settings);

        // Send the token via email
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo(email);
        emailRequest.setSubject("Your Telegram token for HabitFlow");
        emailRequest.setMessage(
                "Hi " + username + "!\n\n" +
                        "Here is your Telegram token. It is valid for 24 hours:\n\n" +
                        tgToken + "\n\n" +
                        "Start the bot and enter this token."
        );
        sendEmail(emailRequest);
    }

    /**
     * Primary method for dispatching a notification.
     * 1. Fetches user details (email/ID) from the User Service.
     * 2. Determines the user's preferred notification channel (Email/TG).
     * 3. Sends the notification via the appropriate method.
     *
     * @param username The username of the recipient (used to fetch settings).
     * @param subject The subject of the notification.
     * @param message The body of the notification.
     * @throws NotificationNotFoundException if settings are missing.
     * @throws ForbiddenActionException if the channel is disabled or not confirmed.
     */
    public void notifyUser(String username, String subject, String message) {
        UserDto userDto = userService.getUserByUsername(username);

        NotificationSettings settings = settingsRepo.findByUserIdAndEnabled(userDto.getId(), true)
                .orElseThrow(() -> new NotificationNotFoundException("No notification settings for user "
                        + username));

        // Route the notification
        switch (settings.getChannel()) {
            case EMAIL -> {
                EmailRequest email = new EmailRequest();
                email.setTo(userDto.getEmail());
                email.setSubject(subject);
                email.setMessage(message);
                sendEmail(email);
            }

            case TG -> {
                if (settings.getStatus() == NotificationStatus.CONFIRMED) {
                    String tgMessage = subject + ": " + message;
                    // The address field contains the confirmed Telegram Chat ID
                    Long chatId = Long.valueOf(settings.getAddress());
                    telegramBotService.sendMessage(chatId, tgMessage);
                } else {
                    settings.setStatus(NotificationStatus.FAILED);
                    settingsRepo.save(settings);
                    throw new ForbiddenActionException("Telegram channel not confirmed for user " + username);
                }
            }
            case NONE -> throw new ForbiddenActionException("Notifications disabled for user " + username);
        }
    }

    /**
     * Sets the email channel status to CONFIRMED. Typically called after a user
     * successfully verifies their email address via a verification link.
     *
     * @param userId The ID of the user.
     * @param email The confirmed email address.
     * @throws NotificationNotFoundException if settings are not found.
     */
    public void confirmEmailChannel(Long userId, String email) {
        NotificationSettings settings = settingsRepo.findByUserIdAndEnabled(userId, true)
                .orElseThrow(() -> new NotificationNotFoundException("Notification settings not found"));

        settings.setChannel(NotificationChannel.EMAIL);
        settings.setAddress(email);
        settings.setStatus(NotificationStatus.CONFIRMED);
        settings.setUpdatedAt(LocalDateTime.now());
        settingsRepo.save(settings);
    }

    /**
     * Deletes all notification settings associated with a list of user IDs.
     * This method is typically called by the {@link UserCleanupConsumer}.
     *
     * @param userIds The list of users whose data should be deleted.
     */
    @Transactional
    public void deleteNotificationsByUserIds(List<Long> userIds) {
        settingsRepo.deleteAllByUserIdIn(userIds);
        System.out.println("[NotificationService] 🧹 Deleted " + userIds.size()
                + " notifications by ID batch.");
    }
}