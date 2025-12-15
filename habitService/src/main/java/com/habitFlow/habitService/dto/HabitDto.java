package com.habitFlow.habitService.dto;

import com.habitFlow.habitService.model.enums.Frequency;
import com.habitFlow.habitService.model.enums.HabitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing a user's full habit configuration and status.
 * This DTO is used primarily for transferring complete habit details upon retrieval
 * or after creation/update.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "HabitDto", description = "Represents a user's habit with all its details")
public class HabitDto {

    @Schema(description = "Unique habit ID", example = "1")
    private Long id;

    @Schema(description = "User ID who owns the habit", example = "1")
    private Long userId;

    @Schema(description = "Title of the habit", example = "Morning workout")
    private String title;

    @Schema(description = "Detailed description of the habit", example = "Do 15 minutes of stretching" +
            " every morning")
    private String description;

    @Schema(description = "Frequency of the habit (DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY)",
            example = "DAILY")
    private Frequency frequency;

    @Schema(description = "Date when the habit starts", example = "2025-10-04")
    private LocalDate startDate;

    @Schema(description = "Date when the habit ends (optional)", example = "2025-11-04")
    private LocalDate endDate;

    @Schema(description = "Current status of the habit (ACTIVE, COMPLETED, ARCHIVED)", example = "ACTIVE")
    private HabitStatus status;
}