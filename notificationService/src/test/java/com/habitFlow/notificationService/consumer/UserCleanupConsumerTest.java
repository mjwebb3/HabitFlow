package com.habitFlow.notificationService.consumer;

import com.habitFlow.Kafka.UserCleanupEvent;
import com.habitFlow.notificationService.service.NotificationService;
import com.habitFlow.notificationService.service.UserCleanupConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Integration tests for the {@link UserCleanupConsumer}.
 * Focuses on verifying that the consumer correctly processes different formats
 * of the UserCleanupEvent (multiple IDs vs. single ID) and delegates the deletion
 * task to the {@link NotificationService}, specifically for GDPR-related cleanup
 * or user deletion events received via Kafka.
 */
@SpringBootTest
class UserCleanupConsumerTest {

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private UserCleanupConsumer consumer;

    @Test
    @DisplayName("✅ ConsumeUserCleanup - Multiple IDs for batch deletion")
    void testConsumeUserCleanup_MultipleIds() {
        UserCleanupEvent event = UserCleanupEvent.builder()
                .userIds(List.of(1L, 2L, 3L))
                .reason("test")
                .build();

        consumer.consumeUserCleanup(event);

        verify(notificationService)
                .deleteNotificationsByUserIds(List.of(1L, 2L, 3L));
    }

    @Test
    @DisplayName("✅ ConsumeUserCleanup - Single ID deletion")
    void testConsumeUserCleanup_SingleId() {
        UserCleanupEvent event = UserCleanupEvent.builder()
                .singleUserId(99L)
                .reason("delete")
                .build();

        consumer.consumeUserCleanup(event);

        verify(notificationService)
                .deleteNotificationsByUserIds(List.of(99L));
    }

    @Test
    @DisplayName("❌ ConsumeUserCleanup - Empty event (no action taken)")
    void testConsumeUserCleanup_Empty() {
        UserCleanupEvent event = UserCleanupEvent.builder().build();

        consumer.consumeUserCleanup(event);

        verify(notificationService, never())
                .deleteNotificationsByUserIds(any());
    }
}