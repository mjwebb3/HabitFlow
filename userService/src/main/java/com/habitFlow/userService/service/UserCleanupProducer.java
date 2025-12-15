package com.habitFlow.userService.service;

import com.habitFlow.Kafka.UserCleanupEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka producer responsible for sending messages about user deletions
 * to the ‘user-cleanup-events’ topic. These messages are used by other services
 * to clean up related data.
 */
@Component
@RequiredArgsConstructor
public class UserCleanupProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "user-cleanup-events";

    /**
     * Sends a cleanup event containing a list of user IDs.
     * Used when batch deleting inactive or unconfirmed users.
     *
     * @param userIds List of deleted user IDs.
     */
    public void sendUsersDeleted(List<Long> userIds) {
        UserCleanupEvent event = UserCleanupEvent.builder()
                .userIds(userIds)
                .reason("Batch user cleanup")
                .source("user-service")
                .build();

        kafkaTemplate.send(TOPIC, event);

        System.out.println("[UserCleanupProducer] 🧹 Sent event: " + event);
    }

    /**
     * Sends a cleanup event for a single user.
     * Used when a user deletes their own account.
     *
     * @param userId ID of the deleted user.
     */
    public void sendSingleUserDeleted(Long userId) {
        UserCleanupEvent event = UserCleanupEvent.builder()
                .singleUserId(userId)
                .reason("Single user self-deletion")
                .source("user-service")
                .build();

        kafkaTemplate.send(TOPIC, event);
        System.out.println("[UserCleanupProducer] 🧹 Sent single cleanup event: " + event);
    }
}