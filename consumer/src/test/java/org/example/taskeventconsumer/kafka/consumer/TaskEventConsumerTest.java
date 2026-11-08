package org.example.taskeventconsumer.kafka.consumer;

import org.example.taskeventconsumer.kafka.dto.TaskEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskEventConsumerTest {

    @Mock
    private TaskEventAuditRepository repository;

    @InjectMocks
    private TaskEventConsumer consumer;

    @Test
    void savesReceivedTaskEvent() {
        TaskEvent event = new TaskEvent(UUID.randomUUID(), "TASK_CREATED", 1L, "Task",
                false, "WORK", 2L, 3L, LocalDateTime.now());

        consumer.consume(event);

        verify(repository).insertIfAbsent(event);
    }
}
