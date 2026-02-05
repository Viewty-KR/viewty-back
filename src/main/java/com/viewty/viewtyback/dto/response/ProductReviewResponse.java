package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.Review;
import com.viewty.viewtyback.entity.ProductCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImgUrl;
    private String productPrice;
    private Long categoryId;
    private String categoryName;
    private String categoryCode;
    private String name;
    private String content;
    private int rating;
    private LocalDateTime createdAt;

    public static ProductReviewResponse from(Review review) {
        Product product = review.getProduct();
        ProductCategory category = product == null ? null : product.getCategoryId();
        return ProductReviewResponse.builder()
                .id(review.getId())
                .productId(product == null ? null : product.getId())
                .productName(product == null ? null : product.getName())
                .productImgUrl(product == null ? null : product.getImgUrl())
                .productPrice(product == null ? null : product.getPrice())
                .categoryId(category == null ? null : category.getId())
                .categoryName(category == null ? null : category.getName())
                .categoryCode(category == null ? null : category.getCateCode())
                .name(review.getName())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
