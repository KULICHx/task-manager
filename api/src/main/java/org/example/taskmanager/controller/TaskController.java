package org.example.taskmanager.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.taskmanager.dto.task.TaskRequest;
import org.example.taskmanager.dto.task.TaskResponse;
import org.example.taskmanager.entity.TaskType;
import org.example.taskmanager.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<TaskResponse> getAll() {
        return taskService.getAll();
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @GetMapping("/by-category/{categoryId}")
    public List<TaskResponse> getByCategoryId(@PathVariable Long categoryId) {
        return taskService.getByCategoryId(categoryId);
    }

    @GetMapping("/by-category-name")
    public List<TaskResponse> getByCategoryName(@RequestParam String name) {
        return taskService.getByCategoryName(name);
    }

    @GetMapping("/by-type")
    public List<TaskResponse> getByType(@RequestParam TaskType type) {
        return taskService.getByType(type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@RequestBody @Valid TaskRequest request) {
        return taskService.create(request);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @RequestBody @Valid TaskRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    @GetMapping("/paged")
    public Page<TaskResponse> getAllPaged(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return taskService.getAllPaged(pageable);
    }

    @GetMapping("/filter/status")
    public List<TaskResponse> getByCompleted(@RequestParam boolean completed) {
        return taskService.getByCompleted(completed);
    }

    @GetMapping("/filter/date")
    public List<TaskResponse> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return taskService.getByCreatedAtBetween(from, to);
    }

    @GetMapping("/filter")
    public List<TaskResponse> getByStatusAndDate(
            @RequestParam boolean completed,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return taskService.getByCompletedAndCreatedAtBetween(completed, from, to);
    }

    @GetMapping("/search")
    public List<TaskResponse> search(@RequestParam String title) {
        return taskService.searchByTitle(title);
    }
}