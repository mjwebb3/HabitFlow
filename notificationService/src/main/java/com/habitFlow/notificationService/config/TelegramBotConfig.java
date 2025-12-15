package com.habitFlow.notificationService.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class for initializing and providing the Telegram Bot instance.
 * It reads the required bot token from environment variables or configuration properties.
 */
@Configuration
public class TelegramBotConfig {

    @Value("${TG_TOKEN}")
    private String botToken;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botToken);
    }
}