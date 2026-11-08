package org.example.taskmanager.strategy;

import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;
import org.springframework.stereotype.Component;

@Component
public class WorkTaskStrategy implements TaskTypeStrategy {

    @Override
    public TaskType getType() {
        return TaskType.WORK;
    }

    @Override
    public void apply(Task task) {
        if (task.getDescription() == null || task.getDescription().isBlank()) {
            task.setDescription("Work task without additional description.");
        }
    }
}