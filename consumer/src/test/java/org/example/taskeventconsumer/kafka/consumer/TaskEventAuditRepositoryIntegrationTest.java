package org.example.taskeventconsumer.kafka.consumer;

import java.time.LocalDateTime;
import java.util.UUID;
import org.example.taskeventconsumer.kafka.dto.TaskEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@Testcontainers
class TaskEventAuditRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("task_events_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TaskEventAuditRepository auditRepository;

    @Autowired
    private TaskEventConsumer consumer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void duplicateEventIsStoredOnlyOnce() {
        TaskEvent event = event(UUID.randomUUID(), "TASK_CREATED", "First title");

        assertThat(auditRepository.insertIfAbsent(event)).isTrue();
        assertThat(auditRepository.insertIfAbsent(event)).isFalse();

        assertThat(countForEvent(event.eventId())).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT title FROM task_event_audit WHERE event_id = ?",
                String.class, event.eventId())).isEqualTo("First title");
    }

    @Test
    void eventsForSameTaskWithDifferentIdsAreBothStored() {
        TaskEvent created = event(UUID.randomUUID(), "TASK_CREATED", "Original title");
        TaskEvent updated = event(UUID.randomUUID(), "TASK_UPDATED", "Updated title");

        consumer.consume(created);
        consumer.consume(updated);
        consumer.consume(updated);

        assertThat(countForEvent(created.eventId())).isEqualTo(1);
        assertThat(countForEvent(updated.eventId())).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM task_event_audit WHERE task_id = ? AND event_id IN (?, ?)",
                Integer.class, created.taskId(), created.eventId(), updated.eventId()))
                .isEqualTo(2);
    }

    @Test
    void databaseFailureIsPropagatedFromListener() {
        TaskEvent invalid = event(UUID.randomUUID(), "TASK_CREATED", null);

        assertThatThrownBy(() -> consumer.consume(invalid))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(countForEvent(invalid.eventId())).isZero();
    }

    private int countForEvent(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM task_event_audit WHERE event_id = ?",
                Integer.class, eventId);
    }

    private static TaskEvent event(UUID eventId, String type, String title) {
        return new TaskEvent(eventId, type, 42L, title, false, "WORK", 7L, 8L,
                LocalDateTime.of(2026, 11, 9, 12, 30));
    }
}
