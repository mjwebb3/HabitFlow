package com.habitFlow.habitService.model;

import com.habitFlow.habitService.model.enums.Frequency;
import com.habitFlow.habitService.model.enums.HabitStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a Habit entity stored in the database.
 * This model contains the details and configuration for a single habit
 * created by a user, including its schedule, status, and metadata.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "habit")
@Builder
@Entity
public class Habit {

    /** The unique identifier for the habit. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The ID of the user who owns this habit (foreign key to the User Service). */
    private Long userId;

    /** The title or short name of the habit. */
    private String title;

    /** A detailed description of the habit. */
    private String description;

    /** The frequency with which the habit should be performed (e.g., DAILY, WEEKLY). */
    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    /** The date when the habit tracking officially begins. */
    private LocalDate startDate;

    /** The date when the habit tracking officially ends (optional). */
    private LocalDate endDate;

    /** The current status of the habit (ACTIVE, ARCHIVED, COMPLETED). */
    @Enumerated(EnumType.STRING)
    private HabitStatus status;

    /** The timestamp when the habit was created. */
    private LocalDateTime createdAt;

    /** The timestamp when the habit was last updated. */
    private LocalDateTime updatedAt;
}

