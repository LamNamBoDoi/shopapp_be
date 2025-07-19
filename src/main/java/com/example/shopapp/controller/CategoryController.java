package com.example.shopapp.controller;

import com.example.shopapp.components.LocalizationUtils;
import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.CategoryDTO;
import com.example.shopapp.models.Category;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.CategoryResponse;
import com.example.shopapp.services.Category.ICategoryService;
import com.example.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCategory(
            @Valid @ModelAttribute CategoryDTO categoryDTO,
            BindingResult result) throws IOException {
        if(result.hasErrors()){
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessages);
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
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ){
        Pageable pageable = PageRequest.of(page, limit);
        Page<Category> categoryPage = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categoryPage);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    public ResponseEntity<String> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_CATEGORIES_SUCCESS, id));
    }
}
