package com.habitFlow.notificationService.service;

import com.habitFlow.Kafka.UserCleanupEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Kafka Consumer service dedicated to processing user cleanup and deletion events.
 * This consumer listens to the 'user-cleanup-events' topic and ensures that all
 * associated notification data (e.g., settings, history) for deleted users
 * are properly removed from the Notification Service database, maintaining data integrity
 * across the microservice landscape.
 */
@Service
@RequiredArgsConstructor
public class UserCleanupConsumer {

    private final NotificationService notificationService;

    /**
     * Listens to the 'user-cleanup-events' Kafka topic and processes the {@link UserCleanupEvent}.
     * This method delegates the actual database cleanup logic to the {@link NotificationService}.
     *
     * @param event The cleanup event containing the IDs of users whose data must be deleted.
     */
    @KafkaListener(topics = "user-cleanup-events", groupId = "notification-group")
    public void consumeUserCleanup(UserCleanupEvent event) {
        List<Long> idsToDelete = event.resolveIds();
        if (idsToDelete.isEmpty()) {
            System.out.println("[UserCleanupConsumer] ⚠️ Received cleanup event with no user IDs.");
            return;
        }

        System.out.printf("[UserCleanupConsumer] 🧹 Cleaning notifications for users %s (reason: %s)%n",
                idsToDelete, event.getReason());

        try {
            notificationService.deleteNotificationsByUserIds(idsToDelete);
            System.out.println("[UserCleanupConsumer] ✅ Notifications cleaned successfully for users: " + idsToDelete);
        } catch (Exception e) {
            System.err.printf("[UserCleanupConsumer] ❌ Failed to clean notifications for %s: %s%n",
                    idsToDelete, e.getMessage());
        }
    }
}