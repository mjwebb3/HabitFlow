package com.habitFlow.habitService.controller;

import com.habitFlow.habitService.dto.HabitCreateDto;
import com.habitFlow.habitService.dto.HabitDto;
import com.habitFlow.habitService.dto.HabitUpdateDto;
import com.habitFlow.habitService.service.HabitFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user habits.
 * This controller serves as the entry point for all habit-related operations,
 * utilizing {@link HabitFacade} to handle authentication and business logic.
 * It is secured and includes comprehensive Swagger/OpenAPI documentation.
 */
@RestController
@RequestMapping("/habit")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Habit management", description = "Manage user habits (create, update, delete, view)")
public class HabitController {

    private final HabitFacade habitFacade;

    /**
     * Creates a new habit for the authenticated user.
     *
     * @param dto The validated data for the new habit.
     * @return A ResponseEntity containing the created {@link HabitDto} and HTTP status 200 (OK).
     */
    @Operation(summary = "Create a new habit", description = "Creates a new habit for the authenticated" +
            " user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habit created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid habit data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Access forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @PostMapping
    public ResponseEntity<HabitDto> createHabit(@Valid @RequestBody HabitCreateDto dto) {
        return ResponseEntity.ok(habitFacade.createHabit(dto));
    }

    /**
     * Retrieves all habits belonging to the authenticated user.
     *
     * @return A ResponseEntity containing a list of {@link HabitDto}s and HTTP status 200 (OK).
     */
    @Operation(summary = "Get all habits of current user", description = "Returns all habits belonging to" +
            " the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of habits returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @GetMapping("/me")
    public ResponseEntity<List<HabitDto>> getMyHabits() {
        return ResponseEntity.ok(habitFacade.getMyHabits());
    }

    /**
     * Retrieves a specific habit by its ID, ensuring the habit belongs to the current user.
     *
     * @param id The ID of the habit to retrieve.
     * @return A ResponseEntity containing the {@link HabitDto} and HTTP status 200 (OK).
     */
    @Operation(summary = "Get habit by ID", description = "Returns a specific habit by its ID for the" +
            " current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habit found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid habit ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Access to this habit is forbidden"),
            @ApiResponse(responseCode = "404", description = "Habit not found"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @GetMapping("/{id}")
    public ResponseEntity<HabitDto> getHabit(@PathVariable Long id) {
        return ResponseEntity.ok(habitFacade.getHabit(id));
    }

    /**
     * Updates an existing habit identified by ID, ensuring the habit belongs to the current user.
     *
     * @param id The ID of the habit to update.
     * @param dto The DTO containing the fields to update (supports partial update).
     * @return A ResponseEntity containing the updated {@link HabitDto} and HTTP status 200 (OK).
     */
    @Operation(summary = "Update existing habit", description = "Updates a habit by ID for the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Habit updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid habit ID format or malformed request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access — token is missing or invalid"),
            @ApiResponse(responseCode = "403", description = "User has no permission to update this habit"),
            @ApiResponse(responseCode = "404", description = "Habit not found for given ID"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @PutMapping("/{id}")
    public ResponseEntity<HabitDto> updateHabit(@PathVariable Long id, @RequestBody HabitUpdateDto dto) {
        return ResponseEntity.ok(habitFacade.updateHabit(id, dto));
    }

    /**
     * Deletes a habit identified by ID, ensuring the habit belongs to the current user.
     *
     * @param id The ID of the habit to delete.
     * @return A ResponseEntity with HTTP status 204 (No Content) upon successful deletion.
     */
    @Operation(summary = "Delete habit", description = "Deletes a habit by ID for the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Habit deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid habit ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "403", description = "Access to this habit is forbidden"),
            @ApiResponse(responseCode = "404", description = "Habit not found"),
            @ApiResponse(responseCode = "502", description = "User Service unavailable")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long id) {
        habitFacade.deleteHabit(id);
        return ResponseEntity.noContent().build();
    }
}