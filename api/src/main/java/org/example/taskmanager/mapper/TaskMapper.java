package org.example.taskmanager.mapper;

import org.example.taskmanager.dto.task.TaskResponse;
import org.example.taskmanager.entity.Task;

public class TaskMapper {
    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getUser().getId(),
                task.getUser().getName(),
                task.getCategory().getId(),
                task.getCategory().getName(),
                task.getType(),
                task.getCreatedAt()
        );
    }
}
