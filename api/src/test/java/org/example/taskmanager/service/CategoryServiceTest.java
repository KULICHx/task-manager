package org.example.taskmanager.service;

import org.example.taskmanager.dto.category.CategoryRequest;
import org.example.taskmanager.dto.category.CategoryResponse;
import org.example.taskmanager.entity.Category;
import org.example.taskmanager.exception.ResourceNotFoundException;
import org.example.taskmanager.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("getById should return category response when category exists")
    void getById_ShouldReturnCategoryResponse_WhenCategoryExists() {
        var category = createCategory();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("test");
    }

    @Test
    @DisplayName("getById should throw exception when category does not exist")
    void getById_ShouldThrowException_WhenCategoryDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("create should save category and return response")
    void create_ShouldSaveAndReturnCategoryResponse() {
        var request = new CategoryRequest("test");
        var saved = createCategory();

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.create(request);

        assertThat(response.name()).isEqualTo("test");
    }

    @Test
    @DisplayName("update should modify category and return updated response")
    void update_ShouldUpdateAndReturnCategoryResponse() {
        var existingCategory = createCategory();
        var request = new CategoryRequest("new name");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

        CategoryResponse response = categoryService.update(1L, request);

        assertThat(response.name()).isEqualTo("new name");
        verify(categoryRepository).save(existingCategory);
    }

    @Test
    @DisplayName("delete should remove category when category exists")
    void delete_ShouldRemoveCategory_WhenCategoryExists() {
        var existingCategory = createCategory();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));

        categoryService.delete(1L);

        verify(categoryRepository).delete(existingCategory);
    }

    @Test
    @DisplayName("getAll should return list of all categories")
    void getAll_ShouldReturnListOfCategories() {
        var category1 = createCategory();
        var category2 = Category.builder().id(2L).name("second").build();

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        List<CategoryResponse> responses = categoryService.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("test");
        assertThat(responses.get(1).name()).isEqualTo("second");
    }

    private Category createCategory() {
        return Category.builder()
                .id(1L)
                .name("test")
                .build();
    }
}