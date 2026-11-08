package org.example.taskmanager.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.kafka.dto.TaskEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private static final String TOPIC_NAME = "task-events";
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_DELAY_MS = 1000;

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    private final ScheduledExecutorService retryScheduler = Executors.newSingleThreadScheduledExecutor();

    public void sendTaskCreatedEvent(Task task) {
        sendWithRetry(buildEvent("TASK_CREATED", task), 1);
    }

    public void sendTaskUpdatedEvent(Task task) {
        sendWithRetry(buildEvent("TASK_UPDATED", task), 1);
    }

    private void sendWithRetry(TaskEvent event, int attempt) {
        try {
            kafkaTemplate.send(TOPIC_NAME, event.taskId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka event sent: type={}, taskId={}, attempt={}",
                                    event.eventType(), event.taskId(), attempt);
                            return;
                        }
                        handleFailure(event, attempt, ex);
                    });
        } catch (Exception ex) {
            handleFailure(event, attempt, ex);
        }
    }

    private void handleFailure(TaskEvent event, int attempt, Throwable ex) {
        if (attempt >= MAX_ATTEMPTS) {
            log.error("Kafka send failed permanently after {} attempts: type={}, taskId={}",
                    MAX_ATTEMPTS, event.eventType(), event.taskId(), ex);
            return;
        }

        long delay = BASE_DELAY_MS * attempt; // 1s, 2s, 3s...
        log.warn("Kafka send failed (attempt {}/{}), retrying in {} ms: type={}, taskId={}, error={}",
                attempt, MAX_ATTEMPTS, delay, event.eventType(), event.taskId(), ex.getMessage());

        retryScheduler.schedule(
                () -> sendWithRetry(event, attempt + 1),
                delay,
                TimeUnit.MILLISECONDS
        );
    }

    private TaskEvent buildEvent(String eventType, Task task) {
        return new TaskEvent(
                eventType,
                task.getId(),
                task.getTitle(),
                task.isCompleted(),
                task.getType().name(),
                task.getUser().getId(),
                task.getCategory().getId(),
                LocalDateTime.now()
        );
    }
}