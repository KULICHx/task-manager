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
        task.setCompleted(false);

        if (task.getDescription() == null || task.getDescription().isBlank()) {
            task.setDescription("Срочная задача. Требует приоритетного выполнения.");
        }
    }
}