package com.viewty.viewtyback.controller;

import com.viewty.viewtyback.dto.request.ReviewCreateRequest;
import com.viewty.viewtyback.dto.request.ReviewUpdateRequest;
import com.viewty.viewtyback.dto.response.ApiResponse;
import com.viewty.viewtyback.dto.response.ProductReviewResponse;
import com.viewty.viewtyback.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class  ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ApiResponse<Page<ProductReviewResponse>> getReviews(
            @RequestParam Long productId,
            Pageable pageable
    ) {
        return ApiResponse.success(reviewService.getReviews(productId, pageable));
    }

    @PostMapping
    public ApiResponse<ProductReviewResponse> createReview(
            @RequestParam Long productId,
            @RequestBody @Valid ReviewCreateRequest request
    ) {
        return ApiResponse.success(reviewService.createReview(productId, request));
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<ProductReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewUpdateRequest request
    ) {
        return ApiResponse.success(reviewService.updateReview(reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ApiResponse.success(null);
    }
}
