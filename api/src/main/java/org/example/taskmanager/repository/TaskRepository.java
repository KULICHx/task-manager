package org.example.taskmanager.repository;

import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCategoryId(Long categoryId);

    List<Task> findByCategoryNameIgnoreCase(String categoryName);

    List<Task> findByType(TaskType type);

    List<Task> findByCompleted(boolean completed);

    List<Task> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<Task> findByCompletedAndCreatedAtBetween(
            boolean completed,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
            SELECT t FROM Task t
            WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))
            """)
    List<Task> searchByTitle(@Param("title") String title);
}
