package org.example.taskeventconsumer.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskeventconsumer.kafka.dto.TaskEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final TaskEventAuditRepository auditRepository;

    @KafkaListener(topics = "${app.kafka.events.topic:task-events}")
    public void consume(TaskEvent event) {
        boolean inserted = auditRepository.insertIfAbsent(event);
        if (inserted) {
            log.info("Saved task event: id={}, type={}, taskId={}",
                    event.eventId(), event.eventType(), event.taskId());
        } else {
            log.debug("Skipped duplicated task event: id={}", event.eventId());
        }
    }
}
