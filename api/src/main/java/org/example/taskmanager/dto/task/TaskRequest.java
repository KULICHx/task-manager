package org.example.taskmanager.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.taskmanager.entity.TaskType;

public record TaskRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        @NotNull Boolean completed,
        @NotNull @Positive Long userId,
        @NotNull @Positive Long categoryId,
        @NotNull TaskType type
) {}
