package com.habitFlow.userService;

import com.habitFlow.userService.model.User;
import com.habitFlow.userService.service.CleanupServiceScheduler;
import com.habitFlow.userService.service.UserCleanupProducer;
import com.habitFlow.userService.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CleanupServiceScheduler}. Checks the logic of the cleanup scheduler
 * for inactive and unconfirmed users. Uses mocks
 * for the user service and Kafka producer.
 */
@SpringBootTest
class CleanupServiceSchedulerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private UserCleanupProducer cleanupProducer;

    @Autowired
    private CleanupServiceScheduler cleanupServiceScheduler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        // Kafka mock producers do not perform any actions
        doNothing().when(cleanupProducer).sendSingleUserDeleted(any());
        doNothing().when(cleanupProducer).sendUsersDeleted(any());
    }

    @Test
    @DisplayName("✅ Clean up unconfirmed users - Must delete expired users")
    void cleanupUnverifiedUsers_shouldDeleteExpired() {

        User u1 = User.builder()
                .id(1L)
                .username("user1")
                .email("u1@example.com")
                .lastActiveAt(LocalDateTime.now())
                .password(passwordEncoder.encode("pass"))
                .emailVerified(false)
                .createdAt(LocalDateTime.now().minusHours(50))
                .build();

        when(userService.findAllByEmailVerifiedFalseAndCreatedAtBefore(any()))
                .thenReturn(List.of(u1));

        cleanupServiceScheduler.cleanupUnverifiedUsers();

        verify(userService, times(1)).deleteAllByIds(List.of(1L));
        verify(cleanupProducer, never()).sendUsersDeleted(any());
    }

    @Test
    @DisplayName("✅ Clean up inactive users - Must delete and send Kafka message")
    void cleanupInactiveUsers_shouldDeleteAndSendKafka() {
        
        User u1 = User.builder()
                .id(10L)
                .username("user11")
                .email("u11@example.com")
                .lastActiveAt(LocalDateTime.now().minusDays(400))
                .password(passwordEncoder.encode("pass1"))
                .emailVerified(false)
                .createdAt(LocalDateTime.now().minusHours(50))
                .build();

        when(userService.findAllInactiveSince(any()))
                .thenReturn(List.of(u1));

        cleanupServiceScheduler.cleanupInactiveUsers();

        verify(userService, times(1)).deleteAllByIds(List.of(10L));

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(cleanupProducer, times(1)).sendUsersDeleted(captor.capture());

        assertEquals(List.of(10L), captor.getValue());
    }
}