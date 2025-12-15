package com.habitFlow.habitService.mapper;
import com.habitFlow.habitService.dto.HabitCreateDto;
import com.habitFlow.habitService.dto.HabitDto;
import com.habitFlow.habitService.model.Habit;

/**
 * Utility class (Mapper) for converting between {@link Habit} model entities
 * and their corresponding Data Transfer Objects (DTOs).
 * This class handles mapping for habit retrieval and habit creation, but not
 * for habit updates, as updates often involve partial DTOs or direct entity manipulation.
 */
public class HabitMapper {

    /**
     * Converts a {@link Habit} entity model to a full {@link HabitDto} for external representation.
     *
     * @param habit The {@link Habit} entity to convert.
     * @return The resulting {@link HabitDto}.
     */
    public static HabitDto toDto(Habit habit) {
        return HabitDto.builder()
                .id(habit.getId())
                .userId(habit.getUserId())
                .title(habit.getTitle())
                .description(habit.getDescription())
                .frequency(habit.getFrequency())
                .startDate(habit.getStartDate())
                .endDate(habit.getEndDate())
                .status(habit.getStatus())
                .build();
    }

    /**
     * Converts a {@link HabitCreateDto} into a {@link Habit} entity model for persistence.
     * Note: This method does not set metadata like {@code id}, {@code userId}, or timestamps;
     * these are typically set in the service layer before saving.
     *
     * @param dto The {@link HabitCreateDto} containing initial data.
     * @return The resulting {@link Habit} entity.
     */
    public static Habit ToEntity(HabitCreateDto dto) {
        return Habit.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .frequency(dto.getFrequency())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus())
                .build();
    }
}