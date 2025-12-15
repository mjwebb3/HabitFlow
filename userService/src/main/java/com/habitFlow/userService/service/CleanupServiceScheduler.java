package com.habitFlow.userService.service;

import com.habitFlow.userService.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service component responsible for scheduling and executing periodic cleanup
 * tasks within the user database.
 * This includes deleting unverified accounts and highly inactive accounts.
 */
@Service
@RequiredArgsConstructor
public class CleanupServiceScheduler {

    private final UserService userService;
    private final UserCleanupProducer userCleanupProducer;

    /**
     * Scheduled task to clean up users who have not verified their email addresses
     * within the defined expiry period (currently 48 hours).
     * Runs every hour.
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupUnverifiedUsers() {
        LocalDateTime expiry = LocalDateTime.now().minusHours(48);
        List<User> expiredUsers = userService.findAllByEmailVerifiedFalseAndCreatedAtBefore(expiry);

        if (expiredUsers.isEmpty()) return;

        List<Long> userIds = expiredUsers.stream().map(User::getId).toList();

        userService.deleteAllByIds(userIds);

        System.out.println("[UserCleanupService] Deleted " + userIds.size() + " unverified users");
    }

    /**
     * Scheduled task to clean up accounts that have been inactive for over one year.
     * After deletion, a cleanup event is sent via Kafka to notify other services.
     * Runs every Sunday at 03:00 (3 AM) Berlin time.
     */
    @Scheduled(cron = "0 0 3 * * SUN", zone = "Europe/Berlin")
    public void cleanupInactiveUsers() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);
        List<User> inactiveUsers = userService.findAllInactiveSince(oneYearAgo);
        if (inactiveUsers.isEmpty()) return;

        List<Long> userIds = inactiveUsers.stream().map(User::getId).toList();
        userService.deleteAllByIds(userIds);
        userCleanupProducer.sendUsersDeleted(userIds);

        System.out.println("[UserCleanupService] Deleted " + userIds.size() + " inactive users");
    }
}