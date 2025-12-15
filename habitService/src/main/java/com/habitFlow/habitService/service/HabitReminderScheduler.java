package com.habitFlow.habitService.service;

import com.habitFlow.habitService.config.UserService;
import com.habitFlow.Kafka.HabitReminderEvent;
import com.habitFlow.habitService.dto.UserDto;
import com.habitFlow.habitService.exception.custom.ExternalServiceException;
import com.habitFlow.habitService.model.Habit;
import com.habitFlow.habitService.model.enums.HabitStatus;
import com.habitFlow.habitService.repository.HabitRepository;
import com.habitFlow.habitService.repository.HabitTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service responsible for scheduling and sending daily habit reminders.
 * This scheduler runs daily to identify active habits that have not been completed
 * (tracked) for the current day and sends a reminder event via Kafka to the
 * user associated with the habit.
 */
@Service
@RequiredArgsConstructor
public class HabitReminderScheduler {

    private static final String TOPIC_REMINDER = "habit-reminders";

    private final HabitRepository habitRepository;
    private final HabitTrackingRepository habitTrackingRepository;
    private final HabitReminderProducer kafkaProducer;
    private final UserService userService;

    /**
     * Sends reminders to users every day at 8:00 PM (Europe/Berlin time zone)
     * about uncompleted habits for the current day.
     * The process involves:
     * - Finding all currently active habits.
     * - Finding all habits that have already been tracked today.
     * - Fetching user details for all unique user IDs involved (for mapping ID to username).
     * - Iterating through active habits and sending a reminder if the habit was not tracked today.
     */
    @Scheduled(cron = "0 0 20 * * *", zone = "Europe/Berlin")
    public void sendDailyReminders() {
        LocalDate today = LocalDate.now();

        // Get all active habits
        List<Habit> activeHabits = habitRepository.findByStatus(HabitStatus.ACTIVE);

        // Identify habits that were already tracked today
        List<Long> trackedHabitIds = habitTrackingRepository.findHabitIdsTrackedOnDate(today);
        var trackedSet = Set.copyOf(trackedHabitIds);

        // Collect unique user IDs for batch fetching
        List<Long> userIds = activeHabits.stream()
                .map(Habit::getUserId)
                .distinct()
                .toList();

        // Fetch user details from external service
        Map<Long, UserDto> users;
        try {
            users = userService.getUsersByIds(userIds);
        } catch (ExternalServiceException e) {
            System.err.println("[HabitReminderScheduler] 🚨 Failed to fetch users: " + e.getMessage());
            return;
        }

        // Iterate and send reminders for untracked active habits
        for (Habit habit : activeHabits) {
            // skip if tracked
            if (trackedSet.contains(habit.getId())) continue;

            UserDto user = users.get(habit.getUserId());

            if (user == null || user.getUsername() == null) {
                System.out.printf("[HabitReminderScheduler] ⚠️ Skipping habit '%s' - no valid user found for id %d%n",
                        habit.getTitle(), habit.getUserId());
                continue;
            }

            String username = user.getUsername();

            try {
                HabitReminderEvent event = new HabitReminderEvent(
                        user.getUsername(),
                        habit.getTitle(),
                        "Don’t forget to complete your habit '" + habit.getTitle() + "' today! 💪",
                        LocalDateTime.now());
                //expire manual test
                /*  if(habit.getId()==426){
                    event.setCreatedAt(LocalDateTime.now().minusDays(2));
                    kafkaProducer.send(TOPIC_REMINDER,event);
                 }
                 else {kafkaProducer.send(TOPIC_REMINDER,event);}*/

                kafkaProducer.send(TOPIC_REMINDER,event);
                System.out.printf("[HabitReminderScheduler] 🔔 Reminder sent for habit '%s' to '%s'%n",
                        habit.getTitle(), username);
            } catch (Exception e) {
                System.err.printf("[HabitReminderScheduler] ⚠️ Failed to send reminder for habit '%s': %s%n",
                        habit.getTitle(), e.getMessage());
            }
        }
    }
}