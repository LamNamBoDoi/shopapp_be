package com.example.shopapp.response;

import com.example.shopapp.models.Category;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String thumbnail;

    public static CategoryResponse fromCategory(Category category){
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .thumbnail(category.getThumbnail())
                .build();
    }
}
