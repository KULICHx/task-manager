package org.example.taskmanager.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.kafka.dto.TaskEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final ApplicationEventPublisher eventPublisher;

    public void sendTaskCreatedEvent(Task task) {
        publish("TASK_CREATED", task);
    }

    public void sendTaskUpdatedEvent(Task task) {
        publish("TASK_UPDATED", task);
    }

    public void sendTaskDeletedEvent(Task task) {
        publish("TASK_DELETED", task);
    }

    private void publish(String type, Task task) {
        eventPublisher.publishEvent(new TaskEvent(
                UUID.randomUUID(),
                type,
                task.getId(),
                task.getTitle(),
                task.isCompleted(),
                task.getType().name(),
                task.getUser().getId(),
                task.getCategory().getId(),
                LocalDateTime.now()
        ));
    }
}
