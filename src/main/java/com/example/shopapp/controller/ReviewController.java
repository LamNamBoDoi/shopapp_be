package com.example.shopapp.controller;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.ReviewDTO;
import com.example.shopapp.dtos.WishlistDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.response.ApiResponse;
import com.example.shopapp.response.ReviewResponse;
import com.example.shopapp.response.WishListResponse;
import com.example.shopapp.services.Review.IReviewService;
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
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController extends TranslateMessages {
    private final IReviewService reviewService;
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewDTO reviewDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(ApiResponse.<ReviewResponse>builder()
                        .success(false)
                        .message(translate(MessageKeys.ERROR_MESSAGE))
                        .error(String.join(", ", errorMessages))
                        .build());
            }

            ReviewResponse newReview = reviewService.createReview(reviewDTO);
            return ResponseEntity.ok(ApiResponse.<ReviewResponse>builder()
                    .success(true)
                    .message(translate(MessageKeys.REVIEW_SUCCESS))
                    .payload(newReview)
                    .build());
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<ReviewResponse>builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewByUserId(@PathVariable Long userId) {
        try {
            List<ReviewResponse> responses = reviewService.getReviewByUserId(userId);
            return ResponseEntity.ok(ApiResponse.<List<ReviewResponse>>builder()
                    .success(true)
                    .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                    .payload(responses)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<ReviewResponse>>builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/product/{product_id}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewByProductId(@PathVariable("productId") Long productId) {
        try {
            List<ReviewResponse> responses = reviewService.getReviewByProductId(productId);
            return ResponseEntity.ok(ApiResponse.<List<ReviewResponse>>builder()
                    .success(true)
                    .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                    .payload(responses)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<List<ReviewResponse>>builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable("id") Long id) {
        try {
            ReviewResponse response = reviewService.getReviewById(id);
            return ResponseEntity.ok(ApiResponse.<ReviewResponse>builder()
                    .success(true)
                    .message(translate(MessageKeys.GET_INFORMATION_SUCCESS))
                    .payload(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<ReviewResponse>builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable("id") Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message(translate(MessageKeys.MESSAGE_DELETE_SUCCESS))
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .success(false)
                    .message(translate(MessageKeys.ERROR_MESSAGE))
                    .error(e.getMessage())
                    .build());
        }
    }
}
