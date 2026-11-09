package org.example.taskmanager;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.example.taskmanager.dto.category.CategoryRequest;
import org.example.taskmanager.dto.task.TaskRequest;
import org.example.taskmanager.dto.task.TaskResponse;
import org.example.taskmanager.dto.user.UserRequest;
import org.example.taskmanager.entity.TaskType;
import org.example.taskmanager.kafka.dto.TaskEvent;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.service.CategoryService;
import org.example.taskmanager.service.TaskService;
import org.example.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "app.cache.redis.enabled=false",
        "app.kafka.events.enabled=true"
})
@Testcontainers
class TaskEventTransactionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("task_manager_transactions")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private KafkaTemplate<String, TaskEvent> kafkaTemplate;

    @Test
    void eventIsDispatchedOnlyAfterSuccessfulCommit() {
        TaskRequest request = createRequest();

        when(kafkaTemplate.send(anyString(), anyString(), any(TaskEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        AtomicReference<Long> taskId = new AtomicReference<>();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            TaskResponse response = taskService.create(request);
            taskId.set(response.id());

            verifyNoInteractions(kafkaTemplate);
        });

        assertThat(taskRepository.existsById(taskId.get())).isTrue();
        verify(kafkaTemplate).send(eq("task-events"), eq(taskId.get().toString()),
                any(TaskEvent.class));
    }

    @Test
    void rolledBackTaskIsNotPersistedAndEventIsNotDispatched() {
        TaskRequest request = createRequest();
        long countBefore = taskRepository.count();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            taskService.create(request);
            verifyNoInteractions(kafkaTemplate);

            status.setRollbackOnly();
        });

        assertThat(taskRepository.count()).isEqualTo(countBefore);
        verifyNoInteractions(kafkaTemplate);
    }

    private TaskRequest createRequest() {
        String suffix = UUID.randomUUID().toString();
        Long userId = userService.create(new UserRequest(
                "Transaction Test User", "transaction-" + suffix + "@example.test")).id();
        Long categoryId = categoryService.create(
                new CategoryRequest("Transaction Test Category " + suffix)).id();

        return new TaskRequest(
                "Transaction test task", "Verify Kafka after commit",
                false, userId, categoryId, TaskType.WORK
        );
    }
}
