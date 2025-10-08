package com.example.shopapp.response;

import com.example.shopapp.models.Comment;
import com.example.shopapp.models.Wishlist;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishListResponse {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("product_id")
    private Long productId ;

    public static WishListResponse fromWishlist(Wishlist wishlist) {
        return WishListResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .productId(wishlist.getProduct().getId())
                .build();
    }

    public static List<WishListResponse> fromWishlistList(List<Wishlist> wishlists) {
        if (wishlists == null || wishlists.isEmpty()) {
            return new ArrayList<>();
        }

        return wishlists.stream()
                .map(WishListResponse::fromWishlist)
                .collect(Collectors.toList());
    }

}
