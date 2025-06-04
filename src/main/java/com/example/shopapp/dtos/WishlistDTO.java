package com.example.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistDTO {
    @NotNull(message = "Product ID không được để trống")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "User ID không được để trống")
    @JsonProperty("user_id")
    private Long userId;
}
