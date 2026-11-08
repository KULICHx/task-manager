--liquibase formatted sql
--changeset task-manager:001-create-task-event-audit
CREATE TABLE task_event_audit (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    task_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_task_event_audit_task_id ON task_event_audit(task_id);
