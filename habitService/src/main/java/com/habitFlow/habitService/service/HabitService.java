package com.habitFlow.habitService.service;

import com.habitFlow.Kafka.NotificationEvent;
import com.habitFlow.habitService.config.UserService;
import com.habitFlow.habitService.dto.HabitCreateDto;
import com.habitFlow.habitService.dto.HabitDto;
import com.habitFlow.habitService.dto.HabitUpdateDto;
import com.habitFlow.habitService.dto.UserDto;
import com.habitFlow.habitService.exception.custom.ForbiddenException;
import com.habitFlow.habitService.exception.custom.ResourceNotFoundException;
import com.habitFlow.habitService.mapper.HabitMapper;
import com.habitFlow.habitService.model.Habit;
import com.habitFlow.habitService.model.HabitTracking;
import com.habitFlow.habitService.repository.HabitRepository;
import com.habitFlow.habitService.repository.HabitTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer class responsible for core Habit business logic,
 * including CRUD operations, authorization checks, and handling
 * cross-service communication (Kafka notifications and User Service interaction).
 */
@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserService userService;
    private final HabitTrackingRepository habitTrackingRepository;
    private final HabitReminderProducer kafkaProducer;

    private static final String TOPIC_NOTIFICATIONS = "user-notifications";

    /**
     * Creates a new habit and persists it to the database.
     * Sets the creation metadata (userId, timestamps) and sends a Kafka notification.
     *
     * @param dto The data for the new habit.
     * @param userId The ID of the user creating the habit.
     * @param username The username of the user (for notification payload).
     * @return The created {@link HabitDto}.
     */
    public HabitDto createHabit(HabitCreateDto dto, Long userId, String username) {
        Habit habit = HabitMapper.ToEntity(dto);
        habit.setUserId(userId);
        habit.setCreatedAt(LocalDateTime.now());
        habit.setUpdatedAt(LocalDateTime.now());

        Habit saved = habitRepository.save(habit);

        NotificationEvent notification = new NotificationEvent(
                username,
                "Habit Created",
                "Your Habit '" + dto.getTitle() + "' created successfully.",
                "habit-service"
        );
        kafkaProducer.send(TOPIC_NOTIFICATIONS, notification);

        return HabitMapper.toDto(saved);
    }

    /**
     * Retrieves all habits belonging to a specific user.
     *
     * @param userId The ID of the user whose habits are requested.
     * @return A list of {@link HabitDto}.
     */
    public List<HabitDto> getHabitsByUserId(Long userId) {
        return habitRepository.findByUserId(userId)
                .stream()
                .map(HabitMapper::toDto)
                .toList();
    }

    /**
     * Retrieves a single habit by its ID, ensuring the requesting user owns it.
     *
     * @param id The ID of the habit.
     * @param userId The ID of the user requesting the habit.
     * @return The requested {@link HabitDto}.
     * @throws ResourceNotFoundException if the habit does not exist.
     * @throws ForbiddenException if the user does not own the habit.
     */
    public HabitDto getHabitById(Long id, Long userId) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + id));

        if (!habit.getUserId().equals(userId)) {
            throw new ForbiddenException("You don’t have access to this habit");
        }

        return HabitMapper.toDto(habit);
    }

    /**
     * Updates an existing habit, applying non-null fields from the DTO.
     * Ensures the requesting user owns the habit before applying changes and sends a Kafka notification.
     *
     * @param id The ID of the habit to update.
     * @param dto The DTO containing fields to update.
     * @param userId The ID of the user requesting the update.
     * @param username The username of the user (for notification payload).
     * @return The updated {@link HabitDto}.
     * @throws ResourceNotFoundException if the habit does not exist.
     * @throws ForbiddenException if the user does not own the habit.
     */
    public HabitDto updateHabit(Long id, HabitUpdateDto dto, Long userId, String username) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + id));

        if (!habit.getUserId().equals(userId)) {
            throw new ForbiddenException("You don’t have access to this habit");
        }

        if (dto.getTitle() != null) habit.setTitle(dto.getTitle());
        if (dto.getDescription() != null) habit.setDescription(dto.getDescription());
        if (dto.getFrequency() != null) habit.setFrequency(dto.getFrequency());
        if (dto.getEndDate() != null) habit.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) habit.setStatus(dto.getStatus());

        habit.setUpdatedAt(LocalDateTime.now());
        Habit updated = habitRepository.save(habit);

        NotificationEvent notification = new NotificationEvent(
                username,
                "Habit Updated",
                "Your Habit '" + habit.getTitle() + "' was updated.",
                "habit-service"
        );
        kafkaProducer.send(TOPIC_NOTIFICATIONS, notification);

        return HabitMapper.toDto(updated);
    }

    /**
     * Deletes a habit by its ID using the user ID directly.
     *
     * @param id The ID of the habit to delete.
     * @param userId The ID of the user requesting the deletion.
     * @throws ResourceNotFoundException if the habit does not exist.
     * @throws ForbiddenException if the user does not own the habit.
     */
    @Transactional
    public void deleteHabit(Long id, Long userId) {
        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found with id: " + id));

        if (!habit.getUserId().equals(userId)) {
            throw new ForbiddenException("You don’t have access to delete this habit");
        }

        habitRepository.delete(habit);
    }

    /**
     * Deletes all habits and associated tracking data for a list of user IDs.
     * This method is typically called in response to a Kafka cleanup event
     * when users are permanently deleted from the system. It handles the cascading
     * deletion of {@link HabitTracking} records manually to ensure data integrity
     * or bypass potential Hibernate cascade issues in specific configurations.
     *
     * @param userIds The list of user IDs whose habits should be deleted.
     */
    @Transactional
    public void deleteHabitsByUserIds(List<Long> userIds) {
        List<Habit> habits = habitRepository.findByUserIdIn(userIds);
        if (habits.isEmpty()) {
            System.out.println("[HabitService] ⚠️ No habits found for users: " + userIds);
            return;
        }

        for (Habit habit : habits) {
            // Manually delete tracking records first to enforce cleanup
            List<HabitTracking> trackings = habitTrackingRepository.findByHabitId(habit.getId());
            if (!trackings.isEmpty()) {
                habitTrackingRepository.deleteAll(trackings);
            }

            // Then delete the habit itself
            habitRepository.delete(habit);
            System.out.println("[HabitService] 🧹 Deleted habit " + habit.getId() + " for user "
                    + habit.getUserId());
        }
    }
}