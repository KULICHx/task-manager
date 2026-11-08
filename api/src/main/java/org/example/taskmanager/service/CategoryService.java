package org.example.taskmanager.service;

import org.example.taskmanager.dto.category.CategoryRequest;
import org.example.taskmanager.dto.category.CategoryResponse;
import org.example.taskmanager.entity.Category;
import org.example.taskmanager.exception.ResourceNotFoundException;
import org.example.taskmanager.mapper.CategoryMapper;
import org.example.taskmanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(CategoryMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return CategoryMapper.toResponse(findEntity(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        var category = Category.builder()
                .name(request.name())
                .build();
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public CategoryResponse update(Long id, CategoryRequest request) {
        var category = findEntity(id);
        category.setName(request.name());
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public void delete(Long id) {
        categoryRepository.delete(findEntity(id));
    }

    @Transactional(readOnly = true)
    public Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}
