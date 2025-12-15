package com.habitFlow.habitService.service;

import com.habitFlow.Kafka.NotificationEvent;
import com.habitFlow.habitService.config.UserService;
import com.habitFlow.habitService.dto.HabitTrackingDto;
import com.habitFlow.habitService.dto.UserDto;
import com.habitFlow.habitService.exception.custom.ForbiddenException;
import com.habitFlow.habitService.exception.custom.ResourceNotFoundException;
import com.habitFlow.habitService.mapper.HabitTrackingMapper;
import com.habitFlow.habitService.model.Habit;
import com.habitFlow.habitService.model.HabitTracking;
import com.habitFlow.habitService.repository.HabitRepository;
import com.habitFlow.habitService.repository.HabitTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer class handling the core business logic for Habit Tracking.
 * It manages CRUD operations for tracking records, ensuring authorization checks
 * (verifying the current user owns the habit) and interacting with repositories,
 * the external {@link UserService}, and the Kafka producer for notifications.
 */
@Service
@RequiredArgsConstructor
public class HabitTrackingService {

    private final HabitTrackingRepository habitTrackingRepository;
    private final HabitRepository habitRepository;
    private final UserService userService;
    private final HabitReminderProducer kafkaProducer;

    private static final String TOPIC_NOTIFICATIONS = "user-notifications";

    /**
     * Creates a new habit tracking record for a specific habit.
     * Performs authorization checks to ensure the user owns the habit before creation.
     * Sends a Kafka notification upon successful creation.
     *
     * @param username The authenticated username (used for authorization).
     * @param habitId The ID of the habit to track.
     * @param dto The data transfer object containing tracking details (e.g., date, done status).
     * @return The saved {@link HabitTrackingDto}.
     * @throws ResourceNotFoundException if the habit does not exist.
     * @throws ForbiddenException if the user does not own the habit.
     */
    public HabitTrackingDto createTracking(String username, Long habitId, HabitTrackingDto dto) {
        UserDto userdto = userService.getUserByUsername(username);

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + habitId));

        if (!habit.getUserId().equals(userdto.getId())) {
            throw new ForbiddenException("You cannot add tracking for this habit");
        }

        HabitTracking tracking = HabitTrackingMapper.toEntity(dto);
        tracking.setHabit(habit);
        HabitTracking saved = habitTrackingRepository.save(tracking);

        NotificationEvent notification = new NotificationEvent(
                username,
                "New Habit Tracking",
                "You added a new tracking for habit '" + habit.getTitle() + "' on " + dto.getTrackDate(),
                "habit-tracking-service"
        );
        kafkaProducer.send(TOPIC_NOTIFICATIONS, notification);

        return HabitTrackingMapper.toDto(saved);
    }

    /**
     * Retrieves all tracking records associated with a specific habit ID.
     * Performs authorization checks to ensure the user owns the habit before retrieval.
     *
     * @param username The authenticated username (used for authorization).
     * @param habitId The ID of the habit.
     * @return A list of {@link HabitTrackingDto} records for the habit.
     * @throws ResourceNotFoundException if the habit does not exist.
     * @throws ForbiddenException if the user does not own the habit.
     */
    public List<HabitTrackingDto> getTrackingsByHabit(String username, Long habitId) {
        UserDto userdto = userService.getUserByUsername(username);

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + habitId));

        if (!habit.getUserId().equals(userdto.getId())) {
            throw new ForbiddenException("You cannot view trackings of this habit");
        }

        return habitTrackingRepository.findByHabitId(habitId).stream()
                .map(HabitTrackingMapper::toDto)
                .toList();
    }

    /**
     * Retrieves tracking records for a specific habit on a specific date.
     * Performs authorization checks to ensure the user owns the habit before retrieval.
     *
     * @param username The authenticated username (used for authorization).
     * @param habitId The ID of the habit.
     * @param date The date for which tracking records are requested.
     * @return A list of {@link HabitTrackingDto} records for the specified habit and date.
     * @throws ResourceNotFoundException if the habit does not exist.
     * @throws ForbiddenException if the user does not own the habit.
     */
    public List<HabitTrackingDto> getTrackingByDate(String username, Long habitId, LocalDate date) {
        UserDto userdto = userService.getUserByUsername(username);

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + habitId));

        if (!habit.getUserId().equals(userdto.getId())) {
            throw new ForbiddenException("You cannot view tracking of this habit");
        }

        return habitTrackingRepository.findByHabitIdAndTrackDate(habitId, date).stream()
                .map(HabitTrackingMapper::toDto)
                .toList();
    }

    /**
     * Deletes a specific habit tracking record by its ID.
     * Performs authorization checks to ensure the user owns the habit associated with the tracking record.
     *
     * @param username The authenticated username (used for authorization).
     * @param id The ID of the habit tracking record to delete.
     * @throws ResourceNotFoundException if the tracking record does not exist.
     * @throws ForbiddenException if the user does not own the habit associated with the tracking.
     */
    public void deleteTracking(String username, Long id) {
        UserDto userdto = userService.getUserByUsername(username);

        HabitTracking tracking = habitTrackingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HabitTracking not found with id: " + id));

        if (!tracking.getHabit().getUserId().equals(userdto.getId())) {
            throw new ForbiddenException("You cannot delete this tracking");
        }

        habitTrackingRepository.delete(tracking);
    }
}
