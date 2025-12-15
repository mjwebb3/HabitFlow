package com.habitFlow.userService.dto;

import com.habitFlow.Kafka.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO used for an authenticated user to request an update to their
 * preferred notification channel (set to Telegram, None, EMAIL).
 */
@Data
@Schema(description = "Request to update notification channel for the current user")
public class UpdateChannelRequest {

    @NotNull(message = "Channel cannot be null. Allowed values: EMAIL, TG, NONE.")
    @Schema(description = "New notification channel", example = "TG")
    private NotificationChannel channel;
}