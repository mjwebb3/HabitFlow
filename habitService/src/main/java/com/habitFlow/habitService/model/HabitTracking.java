package com.habitFlow.habitService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a Habit Tracking record in the database.
 * This model records a user's attempt or completion status for a specific
 * {@link Habit} on a particular date.
 */
@Entity
@Table(name = "habit_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HabitTracking {

    /** The unique identifier for the tracking record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The habit this tracking record belongs to.
     * Established as a Many-to-One relationship with Habit. The fetch type is LAZY
     * for performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id")
    private Habit habit;

    /** The specific date the tracking occurred. */
    private LocalDate trackDate;

    /** Flag indicating whether the habit was completed (true) or not (false). */
    private boolean done;
}