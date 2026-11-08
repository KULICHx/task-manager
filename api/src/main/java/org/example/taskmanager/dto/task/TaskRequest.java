package org.example.taskmanager.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.taskmanager.entity.TaskType;

public record TaskRequest(
        @NotBlank String title,
        String description,
        @NotNull Boolean completed,
        @NotNull Long userId,
        @NotNull Long categoryId,
        @NotNull TaskType type
) {}
