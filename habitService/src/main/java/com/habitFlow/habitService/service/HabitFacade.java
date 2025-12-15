package com.habitFlow.habitService.service;

import com.habitFlow.habitService.config.UserService;
import com.habitFlow.habitService.dto.HabitCreateDto;
import com.habitFlow.habitService.dto.HabitDto;
import com.habitFlow.habitService.dto.HabitUpdateDto;
import com.habitFlow.habitService.dto.UserDto;
import com.habitFlow.habitService.exception.custom.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Facade class that acts as a secure, high-level entry point for habit management.
 * This class handles the extraction of the authenticated user's ID and username
 * from the Spring Security Context and the external {@link UserService},
 * delegating the core business logic to {@link HabitService}.
 */
@Component
@RequiredArgsConstructor
public class HabitFacade {

    private final HabitService habitService;
    private final UserService userService;

    /**
     * Retrieves the username from the current Spring security context.
     *
     * @return The username.
     */
    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Retrieves the ID of the currently authenticated user by calling the external UserService.
     *
     * @return The user ID (Long).
     * @throws ResourceNotFoundException if the user is not found in the external service.
     */
    private Long getUserId() {
        String username = getUsername();
        UserDto user = userService.getUserByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        return user.getId();
    }

    /**
     * Creates a new habit for the authenticated user.
     *
     * @param dto The data for the new habit.
     * @return The created {@link HabitDto}.
     */
    public HabitDto createHabit(HabitCreateDto dto) {
        String username = getUsername();
        Long userId = getUserId();
        return habitService.createHabit(dto, userId, username);
    }

    /**
     * Retrieves all habits belonging to the authenticated user.
     *
     * @return A list of the user's {@link HabitDto}s.
     */
    public List<HabitDto> getMyHabits() {
        Long userId = getUserId();
        return habitService.getHabitsByUserId(userId);
    }

    /**
     * Retrieves a specific habit, ensuring it belongs to the authenticated user.
     *
     * @param id The ID of the habit to retrieve.
     * @return The requested {@link HabitDto}.
     */
    public HabitDto getHabit(Long id) {
        Long userId = getUserId();
        return habitService.getHabitById(id, userId);
    }

    /**
     * Updates an existing habit, ensuring it belongs to the authenticated user.
     *
     * @param id The ID of the habit to update.
     * @param dto The update data.
     * @return The updated {@link HabitDto}.
     */
    public HabitDto updateHabit(Long id, HabitUpdateDto dto) {
        String username = getUsername();
        Long userId = getUserId();
        return habitService.updateHabit(id, dto, userId, username);
    }

    /**
     * Deletes a specific habit, ensuring it belongs to the authenticated user.
     *
     * @param id The ID of the habit to delete.
     */
    public void deleteHabit(Long id) {
        Long userId = getUserId();
        habitService.deleteHabit(id, userId);
    }
}