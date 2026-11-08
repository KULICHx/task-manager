package org.example.taskmanager.mapper;

import org.example.taskmanager.dto.category.CategoryResponse;
import org.example.taskmanager.entity.Category;

public class CategoryMapper {
    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
