package org.example.taskmanager.service;

import org.example.taskmanager.dto.task.TaskRequest;
import org.example.taskmanager.dto.task.TaskResponse;
import org.example.taskmanager.entity.Category;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;
import org.example.taskmanager.entity.User;
import org.example.taskmanager.exception.ResourceNotFoundException;
import org.example.taskmanager.kafka.producer.TaskEventProducer;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.strategy.TaskTypeStrategyContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private TaskEventProducer taskEventProducer;

    @Mock
    private TaskTypeStrategyContext taskTypeStrategyContext;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("getById should return task response when task exists")
    void getById_ShouldReturnTaskResponse_WhenTaskExists() {
        var task = createTask();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("test");
        assertThat(response.type()).isEqualTo(TaskType.WORK);

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("create should save task and send kafka event")
    void create_ShouldSaveTaskAndSendKafkaEvent() {
        var user = createUser();
        var category = createCategory();

        var request = new TaskRequest(
                "test",
                "desc",
                false,
                1L,
                1L,
                TaskType.WORK
        );

        var savedTask = createTask();

        when(userService.findEntity(1L)).thenReturn(user);
        when(categoryService.findEntity(1L)).thenReturn(category);
        when(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class)))
                .thenReturn(savedTask);

        TaskResponse response = taskService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("test");
        assertThat(response.description()).isEqualTo("desc");
        assertThat(response.completed()).isFalse();

        verify(userService).findEntity(1L);
        verify(categoryService).findEntity(1L);
        verify(taskTypeStrategyContext).apply(org.mockito.ArgumentMatchers.any(Task.class));
        verify(taskRepository).save(org.mockito.ArgumentMatchers.any(Task.class));
        verify(taskEventProducer).sendTaskCreatedEvent(savedTask);
    }

    @Test
    @DisplayName("findEntity should throw exception when task does not exist")
    void findEntity_ShouldThrowException_WhenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findEntity(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task not found: 99");

        verify(taskRepository).findById(99L);
    }

    @Test
    @DisplayName("update should modify task, apply strategy and send kafka event")
    void update_ShouldUpdateTaskAndSendKafkaEvent() {
        var existingTask = createTask();
        var user = createUser();
        var category = createCategory();

        var request = new TaskRequest(
                "updated title",
                "updated desc",
                true,
                1L,
                1L,
                TaskType.URGENT
        );

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(userService.findEntity(1L)).thenReturn(user);
        when(categoryService.findEntity(1L)).thenReturn(category);
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        TaskResponse response = taskService.update(1L, request);

        assertThat(response.title()).isEqualTo("updated title");
        assertThat(response.description()).isEqualTo("updated desc");
        assertThat(response.completed()).isTrue();

        verify(taskTypeStrategyContext).apply(existingTask);
        verify(taskRepository).save(existingTask);
        verify(taskEventProducer).sendTaskUpdatedEvent(existingTask);
    }

    @Test
    @DisplayName("delete should remove task when task exists")
    void delete_ShouldRemoveTask_WhenTaskExists() {
        var existingTask = createTask();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        taskService.delete(1L);

        verify(taskRepository).delete(existingTask);
    }

    @Test
    @DisplayName("getAll should return list of all tasks")
    void getAll_ShouldReturnAllTasks() {
        var task = createTask();

        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("test");
    }

    @Test
    @DisplayName("getByCategoryId should return tasks for given category")
    void getByCategoryId_ShouldReturnTasksForCategory() {
        var task = createTask();

        when(taskRepository.findByCategoryId(1L)).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getByCategoryId(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByCategoryName should return tasks for given category name")
    void getByCategoryName_ShouldReturnTasksForCategoryName() {
        var task = createTask();

        when(taskRepository.findByCategoryNameIgnoreCase("string")).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getByCategoryName("string");

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("getByType should return tasks of given type")
    void getByType_ShouldReturnTasksOfType() {
        var task = createTask();

        when(taskRepository.findByType(TaskType.WORK)).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getByType(TaskType.WORK);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).type()).isEqualTo(TaskType.WORK);
    }

    @Test
    @DisplayName("getAllPaged should return page of tasks")
    void getAllPaged_ShouldReturnPagedTasks() {
        var task = createTask();
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(task)));

        var page = taskService.getAllPaged(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).title()).isEqualTo("test");
    }

    @Test
    @DisplayName("getByCompleted should return tasks matching completion status")
    void getByCompleted_ShouldReturnMatchingTasks() {
        var task = createTask();

        when(taskRepository.findByCompleted(false)).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getByCompleted(false);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).completed()).isFalse();
    }

    @Test
    @DisplayName("getByCreatedAtBetween should return tasks within date range")
    void getByCreatedAtBetween_ShouldReturnTasksInRange() {
        var task = createTask();
        var from = LocalDateTime.now().minusDays(1);
        var to = LocalDateTime.now().plusDays(1);

        when(taskRepository.findByCreatedAtBetween(from, to)).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getByCreatedAtBetween(from, to);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("getByCompletedAndCreatedAtBetween should return matching tasks")
    void getByCompletedAndCreatedAtBetween_ShouldReturnMatchingTasks() {
        var task = createTask();
        var from = LocalDateTime.now().minusDays(1);
        var to = LocalDateTime.now().plusDays(1);

        when(taskRepository.findByCompletedAndCreatedAtBetween(false, from, to))
                .thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.getByCompletedAndCreatedAtBetween(false, from, to);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("searchByTitle should return tasks matching title")
    void searchByTitle_ShouldReturnMatchingTasks() {
        var task = createTask();

        when(taskRepository.searchByTitle("test")).thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.searchByTitle("test");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).title()).isEqualTo("test");
    }

    private Task createTask() {
        return Task.builder()
                .id(1L)
                .title("test")
                .description("desc")
                .completed(false)
                .type(TaskType.WORK)
                .createdAt(LocalDateTime.now())
                .user(createUser())
                .category(createCategory())
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .name("string")
                .email("test@mail.com")
                .build();
    }

    private Category createCategory() {
        return Category.builder()
                .id(1L)
                .name("string")
                .build();
    }
}