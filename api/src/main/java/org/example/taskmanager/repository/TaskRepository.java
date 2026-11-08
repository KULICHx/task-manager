package org.example.taskmanager.repository;

import org.example.taskmanager.entity.Task;
import org.example.taskmanager.entity.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Override
    @EntityGraph(attributePaths = {"user", "category"})
    List<Task> findAll();

    @Override
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Task> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "category"})
    Optional<Task> findById(Long id);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Task> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Task> findByCategoryNameIgnoreCase(String categoryName);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Task> findByType(TaskType type);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Task> findByCompleted(boolean completed);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Task> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {"user", "category"})
    List<Task> findByCompletedAndCreatedAtBetween(
            boolean completed,
            LocalDateTime from,
            LocalDateTime to
    );

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("""
            SELECT t FROM Task t
            WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))
            """)
    List<Task> searchByTitle(@Param("title") String title);
}
