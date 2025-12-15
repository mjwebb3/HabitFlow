package com.habitFlow.habitService;

import com.habitFlow.Kafka.HabitReminderEvent;
import com.habitFlow.habitService.config.UserService;
import com.habitFlow.habitService.dto.UserDto;
import com.habitFlow.habitService.exception.custom.ExternalServiceException;
import com.habitFlow.habitService.model.Habit;
import com.habitFlow.habitService.model.enums.HabitStatus;
import com.habitFlow.habitService.repository.HabitRepository;
import com.habitFlow.habitService.repository.HabitTrackingRepository;
import com.habitFlow.habitService.service.HabitReminderProducer;
import com.habitFlow.habitService.service.HabitReminderScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link HabitReminderScheduler}.
 * This class validates the scheduling logic, ensuring reminders are correctly generated
 * and sent via Kafka only for active habits that have not yet been tracked today,
 * and only if corresponding user data can be successfully retrieved.
 */
@SpringBootTest
class HabitReminderSchedulerTest {

    @MockBean
    private HabitRepository habitRepository;

    @MockBean
    private HabitTrackingRepository habitTrackingRepository;

    @MockBean
    private HabitReminderProducer kafkaProducer;

    @MockBean
    private UserService userService;

    @Autowired
    private HabitReminderScheduler scheduler;

    @BeforeEach
    void setup() {
        doNothing().when(kafkaProducer).send(any(), any());
    }

    @Test
    @DisplayName("Should send reminder event for active, untracked habits")
    void sendDailyReminders_shouldSendReminderForUntrackedHabit() {

        Habit h1 = Habit.builder()
                .id(100L)
                .userId(5L)
                .title("Drink Water")
                .status(HabitStatus.ACTIVE)
                .build();

        when(habitRepository.findByStatus(HabitStatus.ACTIVE))
                .thenReturn(List.of(h1));

        when(habitTrackingRepository.findHabitIdsTrackedOnDate(LocalDate.now()))
                .thenReturn(List.of());

        UserDto user = new UserDto(5L, "max", "max@mail.com");
        when(userService.getUsersByIds(List.of(5L)))
                .thenReturn(Map.of(5L, user));

        scheduler.sendDailyReminders();

        ArgumentCaptor<HabitReminderEvent> captor = ArgumentCaptor.forClass(HabitReminderEvent.class);

        verify(kafkaProducer, times(1)).send(eq("habit-reminders"), captor.capture());

        HabitReminderEvent event = captor.getValue();

        assertEquals("max", event.getUsername());
        assertEquals("Drink Water", event.getHabitTitle());
    }

    @Test
    @DisplayName("Should skip sending reminder for habits already tracked today")
    void sendDailyReminders_shouldSkipAlreadyTrackedHabit() {

        Habit h1 = Habit.builder()
                .id(101L)
                .userId(7L)
                .title("Read Book")
                .status(HabitStatus.ACTIVE)
                .build();

        when(habitRepository.findByStatus(HabitStatus.ACTIVE))
                .thenReturn(List.of(h1));

        when(habitTrackingRepository.findHabitIdsTrackedOnDate(LocalDate.now()))
                .thenReturn(List.of(101L));

        when(userService.getUsersByIds(List.of(7L)))
                .thenReturn(Map.of(7L, new UserDto(7L, "katya", "k@mail.com")));

        scheduler.sendDailyReminders();

        verify(kafkaProducer, never()).send(any(), any());
    }

    @Test
    @DisplayName("Should skip reminder generation when the corresponding user is not found")
    void sendDailyReminders_shouldSkipWhenUserNotFound() {

        Habit h1 = Habit.builder()
                .id(102L)
                .userId(777L)
                .title("Stretching")
                .status(HabitStatus.ACTIVE)
                .build();

        when(habitRepository.findByStatus(HabitStatus.ACTIVE))
                .thenReturn(List.of(h1));

        when(habitTrackingRepository.findHabitIdsTrackedOnDate(LocalDate.now()))
                .thenReturn(List.of());

        when(userService.getUsersByIds(List.of(777L)))
                .thenReturn(Map.of());

        scheduler.sendDailyReminders();

        verify(kafkaProducer, never()).send(any(), any());
    }

    @Test
    @DisplayName("Should stop the scheduling job if the external UserService fails")
    void sendDailyReminders_shouldStopIfUserServiceFails() {

        Habit h1 = Habit.builder()
                .id(200L)
                .userId(9L)
                .title("Yoga")
                .status(HabitStatus.ACTIVE)
                .build();

        when(habitRepository.findByStatus(HabitStatus.ACTIVE))
                .thenReturn(List.of(h1));

        when(habitTrackingRepository.findHabitIdsTrackedOnDate(LocalDate.now()))
                .thenReturn(List.of());

        when(userService.getUsersByIds(List.of(9L)))
                .thenThrow(new ExternalServiceException("Service DOWN"));

        scheduler.sendDailyReminders();

        verify(kafkaProducer, never()).send(any(), any());
    }
}