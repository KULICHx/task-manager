package org.example.taskmanager.strategy;

import org.example.taskmanager.entity.Task;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrgentTaskStrategyTest {

    private final UrgentTaskStrategy strategy = new UrgentTaskStrategy();

    @Test
    void doesNotResetCompletedUrgentTask() {
        Task task = Task.builder().completed(true).description("Done").build();

        strategy.apply(task);

        assertThat(task.isCompleted()).isTrue();
    }
}
