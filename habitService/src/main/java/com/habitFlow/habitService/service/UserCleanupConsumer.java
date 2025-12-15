package com.habitFlow.habitService.service;

import com.habitFlow.Kafka.UserCleanupEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Kafka Consumer service dedicated to processing user cleanup and deletion events.
 * This consumer listens to the 'user-cleanup-events' topic and ensures that all
 * associated habit data (all habits and their trackers) for deleted users
 * are properly removed from the Habit Service database, maintaining data integrity
 * across the microservice landscape.
 */
@Service
@RequiredArgsConstructor
public class UserCleanupConsumer {

    private final HabitService habitService;

    /**
     * Listens to the 'user-cleanup-events' Kafka topic and processes the {@link UserCleanupEvent}.
     * This method delegates the actual database cleanup logic to the {@link HabitService}.
     *
     * @param event The cleanup event containing the IDs of users whose data must be deleted.
     */
    @KafkaListener(topics = "user-cleanup-events", groupId = "habit-service-group")
    public void consumeUserCleanup(UserCleanupEvent event) {
        List<Long> userIds = event.resolveIds();

        if (userIds.isEmpty()) {
            System.out.println("[HabitCleanupConsumer] ⚠️ Received cleanup event with no user IDs.");
            return;
        }

        System.out.printf("[HabitCleanupConsumer] 🧹 Received cleanup for users: %s (reason: %s)%n",
                userIds, event.getReason());

        try {
            habitService.deleteHabitsByUserIds(userIds);

            System.out.println("[HabitCleanupConsumer] ✅ Habits deleted for users: " + userIds);
        } catch (Exception e) {
            System.err.printf("[HabitCleanupConsumer] ❌ Failed to delete habits for users %s: %s%n",
                    userIds, e.getMessage());
        }
    }
}