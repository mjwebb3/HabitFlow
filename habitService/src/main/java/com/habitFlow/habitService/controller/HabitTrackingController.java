package com.habitFlow.habitService.controller;

import com.habitFlow.habitService.dto.HabitTrackingDto;
import com.habitFlow.habitService.service.HabitTrackerFacade;
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

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing the tracking of user habits.
 * This controller provides endpoints for creating, retrieving, and deleting
 * habit tracking records, ensuring all actions are authenticated and authorized
 * via the {@link HabitTrackerFacade}.
 */
@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Habit tracking", description = "Track user habits by date and completion")
public class HabitTrackingController {

    private  final HabitTrackerFacade trackingFacade;

    /**
     * Creates a new tracking record for a specific habit.
     *
     * @param habitId The ID of the habit being tracked.
     * @param dto The validated data for the tracking record (date and completion status).
     * @return A ResponseEntity containing the created {@link HabitTrackingDto} and HTTP status 200 (OK).
     */
    @Operation(summary = "Create tracking record", description = "Creates a tracking record for the given habit")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "User has no access to this habit"),
            @ApiResponse(responseCode = "404", description = "Habit not found"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @PostMapping("/habit/{habitId}")
    public ResponseEntity<HabitTrackingDto> createTracking(
            @Parameter(description = "Habit ID", required = true) @PathVariable Long habitId,
            @Valid @RequestBody HabitTrackingDto dto) {

        return ResponseEntity.ok(trackingFacade.createTracking(habitId, dto));
    }

    /**
     * Retrieves all tracking records associated with a specific habit ID.
     *
     * @param habitId The ID of the habit whose tracking records are requested.
     * @return A ResponseEntity containing a list of {@link HabitTrackingDto}s and HTTP status 200 (OK).
     */
    @Operation(summary = "Get all trackings for habit", description = "Returns all tracking records for" +
            " a specific habit")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trackings returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid habit ID parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "User has no access to this habit"),
            @ApiResponse(responseCode = "404", description = "Habit not found"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @GetMapping("/habit/{habitId}")
    public ResponseEntity<List<HabitTrackingDto>> getTrackingsByHabit(
            @Parameter(description = "Habit ID", required = true) @PathVariable Long habitId) {

        return ResponseEntity.ok(trackingFacade.getTrackingsByHabit(habitId));
    }

    /**
     * Retrieves tracking records for a specific habit on a specific date.
     *
     * @param habitId The ID of the habit.
     * @param date The tracking date in YYYY-MM-DD format.
     * @return A ResponseEntity containing a list of {@link HabitTrackingDto}s and HTTP status 200 (OK).
     */
    @Operation(summary = "Get tracking by date", description = "Returns tracking records for a habit on" +
            " a specific date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trackings returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "User has no access to this habit"),
            @ApiResponse(responseCode = "404", description = "Habit not found"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @GetMapping("/habit/{habitId}/date/{date}")
    public ResponseEntity<List<HabitTrackingDto>> getTrackingByDate(
            @Parameter(description = "Habit ID", required = true)
            @PathVariable Long habitId,
            @Parameter(description = "Tracking date (format: YYYY-MM-DD)",
                    required = true) @PathVariable String date) {
        // The date parameter is passed as a String and manually parsed to LocalDate here.
        return ResponseEntity.ok(trackingFacade.getTrackingByDate(habitId, LocalDate.parse(date)));
    }

    /**
     * Deletes a specific tracking record by its ID, ensuring the record belongs to the current user's habit.
     *
     * @param id The ID of the tracking record to delete.
     * @return A ResponseEntity with HTTP status 204 (No Content) upon successful deletion.
     */
    @Operation(summary = "Delete tracking record", description = "Deletes a specific tracking record by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tracking deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tracking ID parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "User has no access to this tracking"),
            @ApiResponse(responseCode = "404", description = "Tracking not found"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTracking(@Parameter(description = "Tracking record ID",
            required = true) @PathVariable Long id) {

        trackingFacade.deleteTracking(id);
        return ResponseEntity.noContent().build();
    }
}
