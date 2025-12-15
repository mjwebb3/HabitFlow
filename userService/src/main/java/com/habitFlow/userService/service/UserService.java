package com.habitFlow.userService.service;

import com.habitFlow.userService.dto.*;
import com.habitFlow.userService.model.User;
import com.habitFlow.userService.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service that manages business logic for the User entity.
 * Responsible for CRUD operations, searching for users by various criteria,
 * and supporting internal processes (e.g., cleaning up inactive users).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Finds a User entity by username.
     *
     * @param username The username to search for.
     * @return A {@link User} object or null if not found.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Finds a User and converts it to a DTO by ID. Used for external requests.
     *
     * @param userId User ID.
     * @return {@link UserDto} object or null if not found.
     */
    public UserDto findUserById(String userId) {
        return userRepository.findById(Long.valueOf(userId))
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .orElse(null);
    }

    /**
     * Finds a User and converts it to a DTO by username. Used for external requests.
     *
     * @param username The username to search for.
     * @return A {@link UserDto} object or null if not found.
     */
    public UserDto findUserDtoByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .orElse(null);
    }

    /**
     * Finds a User entity by email.
     *
     * @param email Email to search for.
     * @return {@link User} object or null if not found.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Finds all users whose email has not been confirmed,
     * and who were created before the specified date/time.
     *
     * @param dateTime Creation deadline.
     * @return List of unconfirmed entities {@link User}.
     */
    public List<User> findAllByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime dateTime) {
        return userRepository.findAllByEmailVerifiedFalseAndCreatedAtBefore(dateTime);
    }

    /**
     * Deletes all users by ID list. The operation is performed transactionally.
     *
     * @param ids List of user IDs to delete.
     */
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        userRepository.deleteAllById(ids);
    }

    /**
     * Checks whether a user exists by ID.
     *
     * @param userId User ID.
     * @return true if the user exists.
     */
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Finds a list of users by their ID and converts them to DTO.
     * Used for internal inter-service requests.
     *
     * @param ids List of user IDs.
     * @return List of {@link  UserDto} objects.
     */
    public List<UserDto> findUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .toList();
    }

    /**
     * Finds all users who have not been active since the specified date.
     *
     * @param cutoffDate The date by which the user should have been active.
     * @return A list of inactive users.
     */
    public List<User> findAllInactiveSince(LocalDateTime cutoffDate) {
        return userRepository.findAllByLastActiveAtBefore(cutoffDate);
    }
}
