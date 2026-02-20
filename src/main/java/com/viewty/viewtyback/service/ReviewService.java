package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.request.ReviewCreateRequest;
import com.viewty.viewtyback.dto.request.ReviewUpdateRequest;
import com.viewty.viewtyback.dto.response.ProductReviewResponse;
import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.Review;
import com.viewty.viewtyback.entity.User;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.ProductRepository;
import com.viewty.viewtyback.repository.ReviewRepository;
import com.viewty.viewtyback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Page<ProductReviewResponse> getReviews(Long productId, Pageable pageable) {
        validateProductExists(productId);
        return reviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId, pageable)
                .map(ProductReviewResponse::from);
    }

    @Transactional
    public ProductReviewResponse createReview(Long productId,
                                                Long userId,
                                              ReviewCreateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Review review = Review.builder()
                .product(product)
                .user(user)
                .content(request.getContent())
                .rating(request.getRating())
                .build();
        Review saved = reviewRepository.save(review);
        return ProductReviewResponse.from(saved);
    }

    @Transactional
    public ProductReviewResponse updateReview(Long reviewId,
                                              Long userId,
                                              ReviewUpdateRequest request) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        review.update(request.getContent(), request.getRating());
        return ProductReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
    }

    private void validateProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    public List<ProductReviewResponse> getMyReviews(Long userId) {
        validateUserExists(userId);
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ProductReviewResponse::from)
                .collect(Collectors.toList());
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }


}
