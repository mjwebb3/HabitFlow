package com.habitFlow.Kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka event used by a Scheduler component (in the Habit Service)
 * to request a reminder notification for a specific habit.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HabitReminderEvent {

    /** The username of the recipient. */
    private String username;

    /** The title of the habit the reminder is for. */
    private String habitTitle;

    /** The reminder message content. */
    private String message;

    /** Timestamp of the event creation, used by consumers for expiration checks if processing is delayed. */
    private LocalDateTime createdAt;
}