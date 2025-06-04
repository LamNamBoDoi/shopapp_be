package com.example.shopapp.response;

import com.example.shopapp.models.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductLiteResponse {
    private Long id;
    private String name;
    private Float price;
    private String thumbnail;

    public static ProductLiteResponse fromProduct(Product product) {
        return ProductLiteResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .build();
    }
}

