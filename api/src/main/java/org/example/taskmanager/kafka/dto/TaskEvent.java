package org.example.taskmanager.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskEvent(
        UUID eventId,
        String eventType,
        Long taskId,
        String title,
        boolean completed,
        String type,
        Long userId,
        Long categoryId,
        LocalDateTime occurredAt
) {}
