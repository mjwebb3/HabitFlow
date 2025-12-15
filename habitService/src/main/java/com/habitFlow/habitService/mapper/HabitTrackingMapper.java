package com.habitFlow.habitService.mapper;

import com.habitFlow.habitService.dto.HabitTrackingDto;
import com.habitFlow.habitService.model.HabitTracking;
import  com.habitFlow.habitService.model.Habit;

/**
 * Utility class (Mapper) for converting between the {@link HabitTracking} model entity
 * and its corresponding Data Transfer Object (DTO).
 * This class focuses on the core tracking fields: {@code id}, {@code trackDate}, and {@code done}.
 * The relationship to the {@link Habit} is handled in the service layer.
 */
public class HabitTrackingMapper {

    /**
     * Converts a {@link HabitTracking} entity model to a {@link HabitTrackingDto} for external
     * representation.
     *
     * @param entity The {@link HabitTracking} entity to convert.
     * @return The resulting {@link HabitTrackingDto}.
     */
    public static HabitTrackingDto toDto(HabitTracking entity) {
        return HabitTrackingDto.builder()
                .id(entity.getId())
                .trackDate(entity.getTrackDate())
                .done(entity.isDone())
                .build();
    }

    /**
     * Converts a {@link HabitTrackingDto} into a {@link HabitTracking} entity model.
     *
     * @param dto The {@link HabitTrackingDto} to convert.
     * @return The resulting {@link HabitTracking} entity.
     */
    public static HabitTracking toEntity(HabitTrackingDto dto) {
        return HabitTracking.builder()
                .id(dto.getId())
                .trackDate(dto.getTrackDate())
                .done(dto.isDone())
                .build();
    }
}
