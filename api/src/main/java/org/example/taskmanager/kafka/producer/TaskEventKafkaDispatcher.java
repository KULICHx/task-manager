package org.example.taskmanager.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskmanager.kafka.dto.TaskEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.events.enabled", havingValue = "true", matchIfMissing = true)
public class TaskEventKafkaDispatcher {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    @Value("${app.kafka.events.topic:task-events}")
    private String topic;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dispatch(TaskEvent event) {
        try {
            kafkaTemplate.send(topic, event.taskId().toString(), event)
                    .whenComplete((result, failure) -> {
                        if (failure != null) {
                            log.error("Failed to publish task event: eventId={}, taskId={}",
                                    event.eventId(), event.taskId(), failure);
                        } else {
                            log.info("Published task event: eventId={}, type={}, taskId={}",
                                    event.eventId(), event.eventType(), event.taskId());
                        }
                    });
        } catch (Exception failure) {
            log.error("Failed to queue task event: eventId={}", event.eventId(), failure);
        }
    }
}
