package org.example.taskmanager.strategy;

import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;

public interface TaskTypeStrategy {

    TaskType getType();

    void apply(Task task);
}