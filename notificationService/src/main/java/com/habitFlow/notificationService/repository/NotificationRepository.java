package com.habitFlow.notificationService.repository;

import com.habitFlow.notificationService.model.NotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link NotificationSettings} entity data.
 */
public interface NotificationRepository extends JpaRepository<NotificationSettings,Long> {
    Optional<NotificationSettings> findByAddress(String address);
    Optional<NotificationSettings> findByUserIdAndEnabled(Long userId, boolean enabled);
    void deleteAllByUserIdIn(List<Long> userIds);
}
