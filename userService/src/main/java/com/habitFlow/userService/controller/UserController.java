package com.habitFlow.userService.controller;

import com.habitFlow.userService.dto.UpdateChannelRequest;
import com.habitFlow.userService.dto.UserDto;
import com.habitFlow.userService.service.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller handling endpoints related to the currently logged-in user's
 * profile management and notification settings. All endpoints require authentication.
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User management", description = "Endpoints for user profile and notification settings")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private  final UserFacade userFacade;

    /**
     * Handles the request to retrieve the profile information for the authenticated user.
     *
     * @return ResponseEntity containing the {@link UserDto}.
     */
    @Operation(summary = "Get current user info", description = "Returns the logged-in user's" +
            " profile info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - token missing or invalid"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe() {
        return userFacade.getCurrentUserInfo();
    }

    /**
     * Handles the request to update the user's preferred notification channel.
     * The request is processed by the facade and results in a Kafka event being sent.
     *
     * @param req The request body containing the new NotificationChannel value.
     * @return ResponseEntity with a success message.
     */
    @Operation(
            summary = "Update current user's notification channel",
            description = "Updates the preferred notification channel for the logged-in user. " +
                    "Available values: EMAIL, TG, NONE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification channel updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing channel value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — token missing or invalid"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while updating channel")
    })
    @PostMapping("/notification-channel")
    public ResponseEntity<String> updateNotificationChannel(
            @Parameter(description = "Request containing new preferred notification channel", required = true)
            @Valid @RequestBody UpdateChannelRequest req
    ) {
        return userFacade.updateNotificationChannel(req);
    }

    /**
     * Triggers the regeneration of a unique Telegram token and queues an email
     * to the user with the new token.
     *
     * @return ResponseEntity with a success message.
     */
    @Operation(summary = "Regenerate Telegram token", description = "Generates a new Telegram token" +
            " and sends it to user's email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Telegram token regenerated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized — token missing or invalid"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while regenerating token")
    })
    @PostMapping("/regenerate-tg-token")
    public ResponseEntity<String> regenerateTelegramToken() {
        return userFacade.regenerateTelegramToken();
    }

    /**
     * Permanently deletes the account of the currently authenticated user and
     * notifies other microservices to clean up related data.
     *
     * @return ResponseEntity with a success message.
     */
    @Operation(
            summary = "Delete your account",
            description = "Deletes your user account and triggers cleanup in other microservices."
    )
    @ApiResponse(responseCode = "200", description = "Account deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/deleteMyData")
    public ResponseEntity<String> deleteMyData() {
        return userFacade.deleteMyData();
    }
}