package com.example.shopapp.services.Category;

import com.example.shopapp.dtos.CategoryDTO;
import com.example.shopapp.models.Category;
import com.example.shopapp.repositories.CategoryRepository;
import com.example.shopapp.services.FileStorageService.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService{
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Category createCategory(CategoryDTO categoryDTO) throws IOException {
        String thumbnail = null;
        if(categoryDTO.getThumbnail() != null && !categoryDTO.getThumbnail().isEmpty()){
            thumbnail = fileStorageService.storeFile(categoryDTO.getThumbnail());
        }
        Category newCategory = Category.builder()
                .name(categoryDTO.getName())
                .thumbnail(thumbnail)
                .build();
        return categoryRepository.save(newCategory);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(
                        ()->new RuntimeException("Category not found")
                );
    }

    @Override
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category updateCategory(Long categoryId, CategoryDTO categoryDTO) throws IOException {
        Category existingCategory = getCategoryById(categoryId);

        existingCategory.setName(categoryDTO.getName());

        if (categoryDTO.getThumbnail() != null && !categoryDTO.getThumbnail().isEmpty()) {
            // Nếu đã có thumbnail cũ, thì xóa file cũ
            String oldThumbnail = existingCategory.getThumbnail();
            if (oldThumbnail != null && !oldThumbnail.isBlank()) {
                fileStorageService.deleteFile(oldThumbnail); // Cần đảm bảo có hàm này
            }

            // Lưu file mới
            String newThumbnail = fileStorageService.storeFile(categoryDTO.getThumbnail());
            existingCategory.setThumbnail(newThumbnail);
        }

        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        //xóa cứng
        categoryRepository.deleteById(id);
    }
}
