package com.habitFlow.habitService.model.enums;

/**
 * Defines the recurrence schedule for a habit, indicating how often a user
 * should attempt to complete the task.
 */
public enum Frequency {
    /**
     * The habit should be performed every day.
     */
    DAILY,

    /**
     * The habit should be performed once per week.
     */
    WEEKLY,

    /**
     * The habit should be performed once every two weeks.
     */
    BIWEEKLY,

    /**
     * The habit should be performed once per month.
     */
    MONTHLY,

    /**
     * The habit should be performed once every three months (four times a year).
     */
    QUARTERLY,

    /**
     * The habit should be performed once per year.
     */
    YEARLY
}

