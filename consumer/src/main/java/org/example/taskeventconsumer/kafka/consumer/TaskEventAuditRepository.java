package org.example.taskeventconsumer.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.example.taskeventconsumer.kafka.dto.TaskEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class TaskEventAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public boolean insertIfAbsent(TaskEvent event) {
        int affected = jdbcTemplate.update("""
                        INSERT INTO task_event_audit (
                            event_id, event_type, task_id, title, completed, task_type,
                            user_id, category_id, occurred_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (event_id) DO NOTHING
                        """,
                event.eventId(), event.eventType(), event.taskId(), event.title(),
                event.completed(), event.type(), event.userId(), event.categoryId(),
                Timestamp.valueOf(event.occurredAt()));
        return affected == 1;
    }
}
