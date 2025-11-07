package com.example.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDTO {
    @NotNull(message = "Product ID không được để trống")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "User ID không được để trống")
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("comment")
    private String comment;

    @Min(value = 1, message = "Rating tối thiểu là 1")
    @Max(value = 5, message = "Rating tối đa là 5")
    @JsonProperty("rating")
    private int rating;

    @JsonProperty("status")
    private String status;
}
