package com.habitFlow.habitService.repository;

import com.habitFlow.habitService.model.Habit;
import com.habitFlow.habitService.model.enums.HabitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for accessing and managing {@link Habit} entity data.
 */
public interface HabitRepository extends JpaRepository<Habit,Long> {
    List<Habit> findByUserId(Long userId);
    List<Habit> findByStatus(HabitStatus status);
    List<Habit> findByUserIdIn(List<Long> userIds);
}
