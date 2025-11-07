package com.example.shopapp.controller;

import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.CategoryDTO;
import com.example.shopapp.models.Category;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.CategoryResponse;
import com.example.shopapp.response.LoginResponse;
import com.example.shopapp.response.user.UserRegisterResponse;
import com.example.shopapp.services.Category.ICategoryService;
import com.example.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController extends TranslateMessages {
    private final ICategoryService categoryService;
    private final LocalizationUtils localizationUtils;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> createCategory(
            @Valid @ModelAttribute CategoryDTO categoryDTO,
            BindingResult result) throws IOException {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message("400")
                    .error(String.join(", ", errorMessages)) // hoặc đưa thẳng list vào payload
                    .build());
        }
        Category newCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok().body(ApiResponse.builder()
                        .success(true)
                        .message(translate(MessageKeys.CREATE_CATEGORIES_SUCCESS))
                        .payload(CategoryResponse.fromCategory(newCategory))
                .build());
    }

    // Hiển thị tất cả category
    @GetMapping("")//http://localhost:8080/api/v1/categories?page=1&limit=10
    public ResponseEntity<ApiResponse<Object>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ){
        Pageable pageable = PageRequest.of(page, limit);
        Page<Category> categoryPage = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .payload(categoryPage)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCategoryById(@PathVariable("id") long categoryId){
        Category existingCategory = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok().body(ApiResponse.builder().success(true)
                .message("Get category by id success")
                .payload(existingCategory).build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute CategoryDTO categoryDTO,
            BindingResult result) throws IOException {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }

        Category updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok().body(ApiResponse.builder()
                .success(true)
                .message(translate(MessageKeys.UPDATE_CATEGORIES_SUCCESS))
                .payload(CategoryResponse.fromCategory(updatedCategory))
                .build());
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.builder().success(true)
                    .message(translate(MessageKeys.DELETE_CATEGORIES_SUCCESS))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .message(translate(MessageKeys.DELETE_CATEGORIES_SUCCESS))
                    .build());
        }
    }
}
