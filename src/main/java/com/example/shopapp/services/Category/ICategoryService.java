package com.example.shopapp.services.Category;

import com.example.shopapp.dtos.CategoryDTO;
import com.example.shopapp.models.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoryService {
    Category createCategory(CategoryDTO category);
    Category getCategoryById(Long id);
    Page<Category> getAllCategories(Pageable pageable);
    Category updateCategory(Long categoryId, CategoryDTO category);
    void deleteCategory(Long id);
}
