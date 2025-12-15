package com.habitFlow.habitService;

import com.habitFlow.Kafka.UserCleanupEvent;
import com.habitFlow.habitService.service.HabitService;
import com.habitFlow.habitService.service.UserCleanupConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Integration tests for {@link UserCleanupConsumer}.
 * This class verifies the consumer's logic for processing {@link UserCleanupEvent} messages
 * received from Kafka, specifically testing its interaction with {@link HabitService}
 * to ensure habit data is deleted when a user is removed from the system.
 */
@SpringBootTest
public class UserCleanupConsumerTest {

    @MockBean
    private HabitService habitService;

    @Autowired
    private UserCleanupConsumer consumer;

    @Test
    @DisplayName("Should call deleteHabitsByUserIds() with correct IDs when event is received")
    void testConsumeUserCleanup_whenUserIdsPresent_callsDelete() {

        UserCleanupEvent event = mock(UserCleanupEvent.class);
        when(event.resolveIds()).thenReturn(List.of(10L, 20L));
        when(event.getReason()).thenReturn("USER_DELETED");

        consumer.consumeUserCleanup(event);

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);

        verify(habitService, times(1)).deleteHabitsByUserIds(captor.capture());

        assertEquals(List.of(10L, 20L), captor.getValue());
    }

    @Test
    @DisplayName("Should skip deletion when the event contains an empty list of IDs")
    void testConsumeUserCleanup_whenEmptyList_doesNothing() {

        UserCleanupEvent event = mock(UserCleanupEvent.class);
        when(event.resolveIds()).thenReturn(List.of());

        consumer.consumeUserCleanup(event);

        verify(habitService, never()).deleteHabitsByUserIds(any());
    }

    @Test
    @DisplayName("Should handle exception gracefully when HabitService fails")
    void testConsumeUserCleanup_whenServiceThrowsException() {

        UserCleanupEvent event = mock(UserCleanupEvent.class);
        when(event.resolveIds()).thenReturn(List.of(1L));
        when(event.getReason()).thenReturn("ERROR_TEST");

        doThrow(new RuntimeException("Failed"))
                .when(habitService)
                .deleteHabitsByUserIds(List.of(1L));

        consumer.consumeUserCleanup(event);

        verify(habitService, times(1))
                .deleteHabitsByUserIds(List.of(1L));
    }
}