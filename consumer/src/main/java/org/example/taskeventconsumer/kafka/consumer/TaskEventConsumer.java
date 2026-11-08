package org.example.taskeventconsumer.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.taskeventconsumer.kafka.dto.TaskEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskEventConsumer {

    @KafkaListener(
            topics = "task-events",
            groupId = "task-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TaskEvent event) {
        log.info("Received event: {}", event);
    }
}