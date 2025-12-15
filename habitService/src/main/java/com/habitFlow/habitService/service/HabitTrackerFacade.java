package com.habitFlow.habitService.service;

import com.habitFlow.habitService.dto.HabitTrackingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Facade class that simplifies access to {@link HabitTrackingService} by automatically
 * injecting the authenticated user's username from the Security Context into service calls.
 * This class abstracts the security context retrieval, making controller methods cleaner.
 */
@Service
@RequiredArgsConstructor
public class HabitTrackerFacade {

    private final HabitTrackingService trackingService;

    /**
     * Retrieves the username from the current Spring security context.
     *
     * @return The username.
     */
    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Creates a new habit tracking record.
     *
     * @param habitId The ID of the habit to track.
     * @param dto The data transfer object containing tracking details.
     * @return The saved {@link HabitTrackingDto}.
     */
    public HabitTrackingDto createTracking(Long habitId, HabitTrackingDto dto) {
        String username = getUsername();
        return trackingService.createTracking(username, habitId, dto);
    }

    /**
     * Retrieves all tracking records for a given habit.
     *
     * @param habitId The ID of the habit.
     * @return A list of {@link HabitTrackingDto} records.
     */
    public List<HabitTrackingDto> getTrackingsByHabit(Long habitId) {
        String username = getUsername();
        return trackingService.getTrackingsByHabit(username, habitId);
    }

    /**
     * Retrieves tracking records for a given habit on a specific date.
     *
     * @param habitId The ID of the habit.
     * @param date The date for which tracking records are requested.
     * @return A list of {@link HabitTrackingDto} records.
     */
    public List<HabitTrackingDto> getTrackingByDate(Long habitId, LocalDate date) {
        String username = getUsername();
        return trackingService.getTrackingByDate(username, habitId, date);
    }

    /**
     * Deletes a specific habit tracking record.
     *
     * @param trackingId The ID of the habit tracking record to delete.
     */
    public void deleteTracking(Long trackingId) {
        String username = getUsername();
        trackingService.deleteTracking(username, trackingId);
    }
}
