package com.example.shopapp.response;

import com.example.shopapp.models.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ProductResponse extends BaseResponse{
    private Long id;
    private String name;
    private Float price;
    private String thumbnail;
    private String description;
    @JsonProperty("product_images")
    private List<ProductImage> productImages;
    @JsonProperty("category_id")
    private Long categoryId;
    @JsonProperty("comments")
    private List<CommentResponse> comments;
    @JsonProperty("wishlists")
    private List<WishListResponse> wishlists;
    @JsonProperty("reviews")
    private List<ReviewResponse> reviews;
    @JsonProperty("average_rating")
    private Double averageRating;
    @JsonProperty("total_reviews")
    private Long totalReviews;



    public static ProductResponse fromProduct(Product product){
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .productImages(product.getProductImages())
                .categoryId(product.getCategory().getId())
                .comments(CommentResponse.fromCommentList(product.getComments()))
                .wishlists(WishListResponse.fromWishlistList(product.getWishlists()))
                .reviews(ReviewResponse.fromReviewList(product.getReviews()))
                .build();
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }
    public static ProductResponse fromProductWithRating(Product product, Double averageRating, Long totalReviews){
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .productImages(product.getProductImages())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .comments(CommentResponse.fromCommentList(product.getComments()))
                .wishlists(WishListResponse.fromWishlistList(product.getWishlists()))
                .reviews(ReviewResponse.fromReviewList(product.getReviews()))
                .averageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .build();

        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }


}
