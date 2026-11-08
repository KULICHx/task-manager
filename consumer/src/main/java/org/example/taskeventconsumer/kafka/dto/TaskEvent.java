package org.example.taskeventconsumer.kafka.dto;

import java.time.LocalDateTime;

public record TaskEvent(
        String eventType,
        Long taskId,
        String title,
        boolean completed,
        String type,
        Long userId,
        Long categoryId,
        LocalDateTime occurredAt
) {
}