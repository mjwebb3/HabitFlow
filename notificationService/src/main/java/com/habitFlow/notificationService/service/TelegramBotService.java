package com.habitFlow.notificationService.service;

import com.habitFlow.Kafka.NotificationChannel;
import com.habitFlow.notificationService.model.NotificationSettings;
import com.habitFlow.notificationService.model.NotificationStatus;
import com.habitFlow.notificationService.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service responsible for initializing and managing the Telegram bot integration.
 * It handles incoming messages (updates) from Telegram, primarily for:
 * 1. Linking a user's HabitFlow account to their Telegram Chat ID using a token.
 * 2. Sending outgoing notifications to specific Telegram Chat IDs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {

    private final TelegramBot telegramBot;
    private final NotificationRepository notificationRepository;

    /**
     * Initializes the Telegram bot listener immediately after the bean is constructed.
     * This method sets up the polling loop to receive incoming updates (messages)
     * from Telegram users.
     * The core logic validates an incoming text message as a verification token
     * and attempts to link it to an existing user's PENDING notification setting.
     */
    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    if (update.message() != null && update.message().text() != null) {
                        String token = update.message().text().trim();
                        Long chatId = update.message().chat().id();

                        // Check if the Chat ID is already linked to prevent reassignment.
                        Optional<NotificationSettings> chatIdOccupied = notificationRepository.findByAddress(chatId.toString());
                        if (chatIdOccupied.isPresent()) {
                            sendMessage(chatId, "This Telegram account is already linked to another user ❌");
                            continue;
                        }

                        // Search for the token (which is temporarily stored in the 'address' field)
                        Optional<NotificationSettings> optSettings = notificationRepository.findByAddress(token);
                        if (optSettings.isPresent()) {

                            // Validate token status and expiration time
                            NotificationSettings settings = optSettings.get();
                            if (settings.getStatus() == NotificationStatus.PENDING
                                    && settings.getExpiryAt().isAfter(LocalDateTime.now())) {

                                // Token is valid: update settings to CONFIRMED
                                settings.setChannel(NotificationChannel.TG);
                                settings.setAddress(chatId.toString()); // Replace the token with the actual Chat ID
                                settings.setStatus(NotificationStatus.CONFIRMED);
                                notificationRepository.save(settings);

                                sendMessage(chatId, "Your Telegram account has been successfully linked! ✅");
                            } else {
                                sendMessage(chatId, "Invalid or expired token. ❌");
                            }
                        } else {
                            sendMessage(chatId, "Invalid token. ❌");
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing telegram update: {}", e.getMessage(), e);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    /**
     * Executes the sending of a text message to a specified Telegram Chat ID.
     *
     * @param chatId The recipient's Telegram Chat ID.
     * @param text The message content to send.
     */
    public void sendMessage(Long chatId, String text) {
        try {
            telegramBot.execute(new SendMessage(chatId, text));
            log.debug("Telegram sent to {}: {}", chatId, text);
        } catch (Exception e) {
            log.error("Failed to send Telegram to {}: {}", chatId, e.getMessage(), e);
        }
    }
}