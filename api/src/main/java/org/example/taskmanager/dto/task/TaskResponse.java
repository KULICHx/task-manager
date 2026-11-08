package org.example.taskmanager.dto.task;

import org.example.taskmanager.entity.TaskType;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        Long userId,
        String userName,
        Long categoryId,
        String categoryName,
        TaskType type,
        LocalDateTime createdAt
) implements Serializable {
}