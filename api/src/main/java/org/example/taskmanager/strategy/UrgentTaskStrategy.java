package org.example.taskmanager.strategy;

import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;
import org.springframework.stereotype.Component;

@Component
public class UrgentTaskStrategy implements TaskTypeStrategy {

    @Override
    public TaskType getType() {
        return TaskType.URGENT;
    }

    @Override
    public void apply(Task task) {
        if (task.getDescription() == null || task.getDescription().isBlank()) {
            task.setDescription("Urgent task requires priority attention.");
        }
    }
}