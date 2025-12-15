package com.habitFlow.habitService.model.enums;

/**
 * Defines the possible states a user's habit can be in.
 * These statuses are primarily used for filtering and determining the habit's current relevance.
 */
public enum HabitStatus {

    /**
     * The habit is currently active and the user is expected to track its completion.
     */
    ACTIVE,

    /**
     * The habit is no longer being actively tracked by the user but is kept for historical reference.
     * This might be a user-driven action.
     */
    ARCHIVED,

    /**
     * The user has successfully completed the habit goal (e.g., finished a 30-day challenge).
     */
    COMPLETED
}