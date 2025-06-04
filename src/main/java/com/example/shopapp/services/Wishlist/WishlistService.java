package com.example.shopapp.services.Wishlist;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.WishlistDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.User;
import com.example.shopapp.models.Wishlist;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.repositories.WishlistRepository;
import com.example.shopapp.response.ProductLiteResponse;
import com.example.shopapp.response.ProductResponse;
import com.example.shopapp.response.UserResponse;
import com.example.shopapp.response.WishListResponse;
import com.example.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService extends TranslateMessages implements IWishlistService{
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    @Override
    public WishListResponse addWishlist(WishlistDTO wishlistDTO) throws DataNotFoundException {
        User user = userRepository.findById(wishlistDTO.getUserId()).orElse(null);
        Product product = productRepository.findById(wishlistDTO.getProductId()).orElse(null);

        if(user == null){
            throw new DataNotFoundException(translate(MessageKeys.USER_NOT_FOUND));
        }
        if(product == null){
            throw new DataNotFoundException(translate(MessageKeys.PRODUCT_NOT_FOUND));
        }
        Wishlist newWishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();
         wishlistRepository.save(newWishlist);
         return WishListResponse.builder()
                 .userId(newWishlist.getUser().getId())
                 .productId(newWishlist.getProduct().getId())
                 .build();
    }

    @Override
    public List<WishListResponse> getWishlistByUserId(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
        return wishlists.stream()
                .map(item->WishListResponse.builder()
                        .id(item.getId())
                        .userId(item.getUser().getId())
                        .productId(item.getProduct().getId())
                        .build()).collect(Collectors.toList());
    }

    @Override
    public List<WishListResponse> getWishlistByProductId(Long productId) {
        List<Wishlist> wishlists = wishlistRepository.findByProductId(productId);
        return wishlists.stream()
                .map(item->WishListResponse.builder()
                        .userId(item.getUser().getId())
                        .productId(item.getProduct().getId())
                        .build()).collect(Collectors.toList());
    }

    @Override
    public WishListResponse getWishlistById(Long id) {
        Wishlist wishlist = wishlistRepository.findById(id).orElse(null);
        assert wishlist != null;
        return WishListResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .productId(wishlist.getProduct().getId())
                .build();
    }

    @Override
    public void deleteWishlist(Long id) {
        wishlistRepository.findById(id).ifPresent(wishlist -> {
            wishlistRepository.deleteById(id);
        });
    }
}
