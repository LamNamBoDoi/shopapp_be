package com.example.shopapp.services.Review;

import com.example.shopapp.components.TranslateMessages;
import com.example.shopapp.dtos.ReviewDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Product;
import com.example.shopapp.models.Review;
import com.example.shopapp.models.User;
import com.example.shopapp.models.Wishlist;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.ReviewRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.repositories.WishlistRepository;
import com.example.shopapp.response.ReviewResponse;
import com.example.shopapp.response.WishListResponse;
import com.example.shopapp.utils.MessageKeys;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReviewService extends TranslateMessages implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponse createReview(ReviewDTO reviewDTO) throws DataNotFoundException {
        User user = userRepository.findById(reviewDTO.getUserId()).orElse(null);
        Product product = productRepository.findById(reviewDTO.getProductId()).orElse(null);

        if(user == null){
            throw new DataNotFoundException(translate(MessageKeys.USER_NOT_FOUND));
        }
        if(product == null){
            throw new DataNotFoundException(translate(MessageKeys.PRODUCT_NOT_FOUND));
        }

        Review newReview = Review.builder()
                .user(user)
                .product(product)
                .status(reviewDTO.getStatus())
                .comment(reviewDTO.getComment())
                .rating(reviewDTO.getRating())
                .build();
        reviewRepository.save(newReview);
        return ReviewResponse.fromReview(newReview);
    }

    @Override
    public List<ReviewResponse> getReviewByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(ReviewResponse::fromReview).collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(ReviewResponse::fromReview).collect(Collectors.toList());
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id).orElse(null);
        assert review != null;
        return ReviewResponse.fromReview(review);
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.findById(id).ifPresent(wishlist -> {
            reviewRepository.deleteById(id);
        });
    }
}
