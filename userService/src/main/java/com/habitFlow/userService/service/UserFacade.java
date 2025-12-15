package com.habitFlow.userService.service;

import com.habitFlow.Kafka.RegenerateTelegramTokenEvent;
import com.habitFlow.Kafka.UpdateNotificationChannelEvent;
import com.habitFlow.userService.dto.UpdateChannelRequest;
import com.habitFlow.userService.dto.UserDto;
import com.habitFlow.userService.exception.custom.InvalidRequestException;
import com.habitFlow.userService.exception.custom.UserNotFoundException;
import com.habitFlow.userService.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserFacade handles business logic related to the currently logged-in user.
 * Abstracts user operations such as retrieving information, updating
 * notification channels, and deleting accounts using services {@link UserService} and
 * Kafka producers {@link NotificationProducer}, {@link UserCleanupProducer}).
 */
@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final UserCleanupProducer cleanupProducer;
    private final NotificationProducer notificationProducer;

    /**
     * Gets basic information (ID, name, email) about the currently authenticated user.
     *
     * @return ResponseEntity with a {@link UserDto} object.
     * @throws UserNotFoundException If the user is not found in the database.
     */
    public ResponseEntity<UserDto> getCurrentUserInfo() {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        if (user == null) throw new UserNotFoundException("User not found");

        return ResponseEntity.ok(new UserDto(user.getId(), user.getUsername(), user.getEmail()));
    }

    /**
     * Requests an update to the notification channel (e.g., Telegram or Email) for
     * the current user by sending an event to Kafka.
     *
     * @param req An {@link UpdateChannelRequest} object containing the new channel.
     * @return ResponseEntity with a string confirmation.
     * @throws InvalidRequestException If the ‘channel’ field is missing from the request.
     * @throws UserNotFoundException If the user is not found.
     */
    public ResponseEntity<String> updateNotificationChannel(UpdateChannelRequest req) {
        if (req.getChannel() == null) {
            throw new InvalidRequestException("Channel is required");
        }

        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        if (user == null) throw new UserNotFoundException("User not found");

        UpdateNotificationChannelEvent event = new UpdateNotificationChannelEvent();
        event.setUserId(user.getId());
        event.setChannel(req.getChannel());

        notificationProducer.sendUpdateChannel(event);

        return ResponseEntity.ok("Notification channel update requested");
    }

    /**
     * Requests the generation of a new Telegram token for the current user,
     * sending an event to Kafka to send the token by email.
     *
     * @return ResponseEntity with string confirmation.
     * @throws UserNotFoundException If the user is not found.
     */
    public ResponseEntity<String> regenerateTelegramToken() {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        if (user == null) throw new UserNotFoundException("User not found");

        RegenerateTelegramTokenEvent event = new RegenerateTelegramTokenEvent();
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setUsername(user.getUsername());

        notificationProducer.sendRegenerateTelegramToken(event);

        return ResponseEntity.ok("A new Telegram token has been sent to your email.");
    }

    /**
     * Retrieves the username from the current Spring security context.
     *
     * @return The username.
     */
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Completely deletes the account of the currently authenticated user and
     * sends an event to Kafka to delete related data in other services.
     *
     * @return ResponseEntity with a string confirmation of successful deletion.
     * @throws UserNotFoundException If the user is not found.
     */
    public ResponseEntity<String> deleteMyData() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        userService.deleteAllByIds(List.of(user.getId()));
        cleanupProducer.sendSingleUserDeleted(user.getId());
        return ResponseEntity.ok("Your account and all related data were successfully deleted.");
    }
}