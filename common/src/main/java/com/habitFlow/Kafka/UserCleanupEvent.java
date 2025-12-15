package com.habitFlow.Kafka;

import lombok.*;

import java.util.List;

/**
 * Kafka event used to signal other microservices (like Habit or Notification Service)
 * to perform cleanup operations related to user data.
 * This event is typically dispatched when a user is deleted, deactivated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCleanupEvent {

    /** A single user ID to be cleaned up (used when userIds is null/empty). */
    private Long singleUserId;

    /** A list of user IDs to be cleaned up (used for bulk operations). */
    private List<Long> userIds;

    /** The reason for the cleanup. */
    private String reason;

    /** The source microservice that initiated the event. */
    private String source;

    /**
     * Resolves and returns a unified list of user IDs from either {@code userIds}
     * or {@code singleUserId}.
     *
     * @return A list of Long IDs to clean up, or an empty list if neither is present.
     */
    public List<Long> resolveIds() {
        if (userIds != null && !userIds.isEmpty()) return userIds;
        return singleUserId != null ? List.of(singleUserId) : List.of();
    }
}
