package com.example.shopapp.services.Wishlist;

import com.example.shopapp.dtos.WishlistDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.response.WishListResponse;

import java.util.List;
import java.util.Optional;

public interface IWishlistService {
    WishListResponse addWishlist(WishlistDTO wishlistDTO) throws DataNotFoundException;

    List<WishListResponse> getWishlistByUserId(Long userId);

    List<WishListResponse> getWishlistByProductId(Long productId);

    WishListResponse getWishlistById(Long id);

    void deleteWishlist(Long id);
}
