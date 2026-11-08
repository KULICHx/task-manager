package org.example.taskmanager.strategy;

import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;
import org.springframework.stereotype.Component;

@Component
public class StudyTaskStrategy implements TaskTypeStrategy {

    @Override
    public TaskType getType() {
        return TaskType.STUDY;
    }

    @Override
    public void apply(Task task) {
        if (task.getDescription() == null || task.getDescription().isBlank()) {
            throw new IllegalArgumentException("For STUDY task description is required");
        }
    }
}