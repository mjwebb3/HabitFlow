package com.habitFlow.notificationService.controller;

import com.habitFlow.notificationService.dto.*;
import com.habitFlow.notificationService.service.NotificationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for the Notification Service's **Internal API**.
 * This controller handles **synchronous requests** only from other **trusted microservices** *
 * (e.g., User Service for settings, Habit Service for dispatch).
 * * NOTE: While this API exists, the primary, asynchronous communication and event-driven
 * interactions with other services (like user cleanup and most configuration updates)
 * are handled via the **Kafka message broker**, not this REST API.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(
        name = "Notification Management",
        description = "Internal API for managing user notification settings and sending messages"
)
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationFacade notificationFacade;

    /**
     * Endpoint to send a direct email message (e.g., for system-initiated verification).
     *
     * @param request The EmailRequest DTO containing the recipient, subject, and message.
     * @return ResponseEntity with a success message.
     */
    @Operation(
            summary = "Send email message",
            description = "Sends an email notification directly to a user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email sent successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Forbidden when invalid service" +
                            " token used"),
                    @ApiResponse(responseCode = "404", description = "Notification settings not found"),
                    @ApiResponse(responseCode = "502", description = "Failed to send email"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
            }
    )
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(
            @Parameter(description = "Email request with recipient and message content",
                    required = true)
            @Valid @RequestBody EmailRequest request) {
        return notificationFacade.sendEmail(request);
    }

    /**
     * Endpoint to create the default notification settings for a new user account.
     *
     * @param request The NotificationSettingsRequest DTO containing initial user data.
     * @return ResponseEntity with status 200 OK.
     */
    @Operation(
            summary = "Create initial notification settings",
            description = "Creates default notification settings for a new user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Settings created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Missing ROLE_SERVICE authority"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/create-settings")
    public ResponseEntity<Void> createSettings(
            @Parameter(description = "Initial notification settings request",
                    required = true)
            @Valid @RequestBody NotificationSettingsRequest request) {
        return notificationFacade.createSettings(request);
    }

    /**
     * Endpoint to change the user's active notification channel (EMAIL, TG, or NONE).
     *
     * @param request The UpdateChannelRequest DTO specifying the user ID and new channel.
     * @return ResponseEntity with status 200 OK.
     */
    @Operation(
            summary = "Update notification channel",
            description = "Changes user’s active notification channel (EMAIL, TELEGRAM, NONE)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Channel updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Missing ROLE_SERVICE authority"),
                    @ApiResponse(responseCode = "404", description = "Notification settings not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/update-channel")
    public ResponseEntity<Void> updateChannel(
            @Parameter(description = "Request containing new notification channel",
                    required = true)
           @Valid @RequestBody UpdateChannelRequest request) {
        return notificationFacade.updateChannel(request);
    }

    /**
     * Endpoint to initiate the Telegram linking process by regenerating and sending
     * a temporary verification token to the user's email.
     *
     * @param request The NotificationSettingsRequest DTO with user details.
     * @return ResponseEntity with a success message.
     */
    @Operation(
            summary = "Regenerate Telegram token",
            description = "Generates a new Telegram verification token for linking user account",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Telegram token regenerated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Missing ROLE_SERVICE authority"),
                    @ApiResponse(responseCode = "403", description = "Telegram channel is not selected"),
                    @ApiResponse(responseCode = "404", description = "Notification settings not found"),
                    @ApiResponse(responseCode = "502", description = "Failed to send email"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/regenerate-tg-token")
    public ResponseEntity<String> regenerateToken(
            @Parameter(description = "Request containing user data for token regeneration",
                    required = true)
            @Valid @RequestBody NotificationSettingsRequest request) {
        return notificationFacade.regenerateToken(request);
    }

    /**
     * Primary dispatch endpoint. Receives a message and routes it to the user's
     * currently active and confirmed channel (Email or Telegram).
     *
     * @param request The DispatchNotificationRequest DTO with target user and message content.
     * @return ResponseEntity with status 200 OK.
     */
    @Operation(
            summary = "Dispatch internal notification",
            description = "Sends a message to user via their active notification channel",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Missing ROLE_SERVICE authority"),
                    @ApiResponse(responseCode = "403", description = "Notification channel not confirmed" +
                            " or disabled"),
                    @ApiResponse(responseCode = "404", description = "Notification settings not found"),
                    @ApiResponse(responseCode = "502", description = "Failed to send notification"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
                    @ApiResponse(responseCode = "502", description = "Failed to fetch user data from User Service")
            }
    )
    @PostMapping("/dispatch")
    public ResponseEntity<Void> dispatchNotification(
            @Parameter(description = "Request describing notification content and target user",
                    required = true)
            @Valid @RequestBody DispatchNotificationRequest request) {
        return notificationFacade.dispatchNotification(request);
    }

    /**
     * Endpoint to confirm the email channel status, typically called by the User Service
     * after a user successfully clicks a verification link.
     *
     * @param request The NotificationSettingsRequest DTO containing the user ID and email.
     * @return ResponseEntity with status 200 OK.
     */
    @Operation(
            summary = "Confirm email channel",
            description = "Marks email as verified after user clicks confirmation link",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email channel confirmed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Missing ROLE_SERVICE authority"),
                    @ApiResponse(responseCode = "404", description = "Notification settings not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error"),
            }
    )
    @PostMapping("/confirm-email")
    public ResponseEntity<Void> confirmEmail(@Valid @RequestBody NotificationSettingsRequest request) {
        return notificationFacade.confirmEmail(request);
    }

}