package org.example.taskmanager.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.taskmanager.dto.task.TaskRequest;
import org.example.taskmanager.dto.task.TaskResponse;
import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;
import org.example.taskmanager.exception.ResourceNotFoundException;
import org.example.taskmanager.kafka.producer.TaskEventProducer;
import org.example.taskmanager.mapper.TaskMapper;
import org.example.taskmanager.repository.TaskRepository;
import org.example.taskmanager.strategy.TaskTypeStrategyContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TaskEventProducer taskEventProducer;
    private final TaskTypeStrategyContext taskTypeStrategyContext;

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll() {
        return taskRepository.findAll().stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse getById(Long id) {
        log.info("Get task by id: {}", id);
        return TaskMapper.toResponse(findEntity(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        log.info("Create task: title={}, userId={}, categoryId={}",
                request.title(), request.userId(), request.categoryId());

        var user = userService.findEntity(request.userId());
        var category = categoryService.findEntity(request.categoryId());

        var task = Task.builder()
                .title(request.title())
                .description(request.description())
                .completed(request.completed())
                .type(request.type())
                .user(user)
                .category(category)
                .build();

        taskTypeStrategyContext.apply(task);

        var saved = taskRepository.save(task);

        taskEventProducer.sendTaskCreatedEvent(saved);

        return TaskMapper.toResponse(saved);
    }

    @Transactional
    @CachePut(value = "tasks", key = "#id")
    public TaskResponse update(Long id, TaskRequest request) {

        log.info("Update task: id={}, title={}, userId={}, categoryId={}",
                id,
                request.title(),
                request.userId(),
                request.categoryId());

        var task = findEntity(id);

        var user = userService.findEntity(request.userId());
        var category = categoryService.findEntity(request.categoryId());

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCompleted(request.completed());
        task.setType(request.type());
        task.setUser(user);
        task.setCategory(category);

        taskTypeStrategyContext.apply(task);

        Task updated = taskRepository.save(task);

        taskEventProducer.sendTaskUpdatedEvent(updated);

        return TaskMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "tasks", key = "#id")
    public void delete(Long id) {
        log.warn("Delete task id={}", id);
        Task task = findEntity(id);
        taskRepository.delete(task);
        taskEventProducer.sendTaskDeletedEvent(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByCategoryId(Long categoryId) {
        return taskRepository.findByCategoryId(categoryId)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByCategoryName(String categoryName) {
        return taskRepository.findByCategoryNameIgnoreCase(categoryName)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByType(TaskType type) {
        return taskRepository.findByType(type)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Task findEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found: id={}", id);
                    return new ResourceNotFoundException("Task not found: " + id);
                });
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllPaged(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(TaskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByCompleted(boolean completed) {
        return taskRepository.findByCompleted(completed)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return taskRepository.findByCreatedAtBetween(from, to)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByCompletedAndCreatedAtBetween(
            boolean completed,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return taskRepository.findByCompletedAndCreatedAtBetween(completed, from, to)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> searchByTitle(String title) {
        return taskRepository.searchByTitle(title)
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }
}