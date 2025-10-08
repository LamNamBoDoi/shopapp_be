package com.example.shopapp.controller;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.WishlistDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.WishListResponse;
import com.example.shopapp.services.Wishlist.IWishlistService;
import com.example.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/wishlists")
@RequiredArgsConstructor
public class WishlistController extends TranslateMessages {
    private final IWishlistService wishlistService;

    @PostMapping("")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> createWishlist(
            @Valid @RequestBody WishlistDTO wishlistDTO,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();

            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(String.join(", ", errorMessages))
                    .build());
        }

        try {
            WishListResponse newWishlist = wishlistService.addWishlist(wishlistDTO);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(translate(MessageKeys.WISHLIST_SUCCESS))
                    .payload(newWishlist)
                    .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> getWishlistByUserId(@PathVariable Long userId) {
        try {
            List<WishListResponse> responses = wishlistService.getWishlistByUserId(userId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                    .payload(responses)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/product/{product_id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> getWishlistByProductId(@PathVariable("product_id") Long productId) {
        try {
            List<WishListResponse> responses = wishlistService.getWishlistByProductId(productId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                    .payload(responses)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> getWishlistById(@PathVariable("id") Long id) {
        try {
            WishListResponse response = wishlistService.getWishlistById(id);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                    .payload(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> deleteWishlist(@PathVariable("id") Long id) {
        try {
            wishlistService.deleteWishlist(id);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(translate(MessageKeys.MESSAGE_DELETE_SUCCESS))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }
}
