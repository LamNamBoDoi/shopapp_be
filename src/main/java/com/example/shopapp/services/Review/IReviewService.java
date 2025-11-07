package com.example.shopapp.services.Review;

import com.example.shopapp.dtos.ReviewDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.response.ReviewResponse;

import java.util.List;

public interface IReviewService {
    ReviewResponse createReview(ReviewDTO reviewDTO) throws DataNotFoundException;

    List<ReviewResponse> getReviewByUserId(Long userId);

    List<ReviewResponse> getReviewByProductId(Long productId);

    ReviewResponse getReviewById(Long id);

    void deleteReview(Long id);
}
