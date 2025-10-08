package com.example.shopapp.response;

import com.example.shopapp.models.Review;
import com.example.shopapp.models.Wishlist;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String username;

    @JsonProperty("rating")
    private int rating;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ReviewResponse fromReview(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .username(review.getUser().getFullName()) // Giả sử User có field username
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public static List<ReviewResponse> fromReviewList(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return new ArrayList<>();
        }

        return reviews.stream()
                .map(ReviewResponse::fromReview)
                .toList();
    }

}
