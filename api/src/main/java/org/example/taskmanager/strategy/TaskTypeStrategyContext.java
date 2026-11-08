package org.example.taskmanager.strategy;

import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskTypeStrategyContext {

    private final Map<TaskType, TaskTypeStrategy> strategies = new EnumMap<>(TaskType.class);

    public TaskTypeStrategyContext(List<TaskTypeStrategy> strategyList) {
        for (TaskTypeStrategy strategy : strategyList) {
            strategies.put(strategy.getType(), strategy);
        }
    }

    public void apply(Task task) {
        TaskTypeStrategy strategy = strategies.get(task.getType());

        if (strategy == null) {
            return;
        }

        strategy.apply(task);
    }
}