package com.viewty.viewtyback.controller;

import com.viewty.viewtyback.dto.response.ApiResponse;
import com.viewty.viewtyback.dto.response.ProductDetailResponse;
import com.viewty.viewtyback.dto.response.ProductListResponse;
import com.viewty.viewtyback.security.CustomUserDetails;
import com.viewty.viewtyback.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final com.viewty.viewtyback.repository.ProductCategoryRepository productCategoryRepository;

    /**
     * 카테고리 전체 목록 조회 (순환 참조 방지를 위해 DTO 사용)
     */
    @GetMapping("/categories")
    public ApiResponse<List<com.viewty.viewtyback.dto.response.CategoryResponse>> getCategories() {
        List<com.viewty.viewtyback.dto.response.CategoryResponse> responses = productCategoryRepository.findAll().stream()
                .map(com.viewty.viewtyback.dto.response.CategoryResponse::from)
                .collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(responses);
    }

    @GetMapping
    public ApiResponse<Page<ProductListResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            // size=10을 기본값으로 설정 (원하는대로 20, 50 등 변경 가능)
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(productService.getProducts(categoryId, pageable));
    }

    /**
     * 사용자 설문 정보 기반 제품 추천
     * keyword가 있으면 설문 정보 대신 키워드로만 검색
     */
    @GetMapping("/recommend")
    public ApiResponse<Page<ProductListResponse>> getRecommendedProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = null;
        if (userDetails != null) {
            userId = userDetails.getId();
        }
        return ApiResponse.success(productService.getRecommendedProducts(userId, keyword, pageable));
    }

//  효능별 상품 조회
    @GetMapping("/functional")
    public ApiResponse<Page<ProductListResponse>> getFunctionalProducts(
            @RequestParam String type,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(productService.getProductsByFunctionalType(type, pageable));
    }

//      상품 상세 조회 (성분 분석 포함)
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable Long id) {
        ProductDetailResponse response = productService.getProductDetail(id);
        return ApiResponse.success(response);
    }

    // AR 체험 가능한 제품 조회
    @GetMapping("/ar-products")
    public ApiResponse<Page<ProductListResponse>> getArProducts(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        System.out.println("AT");
        return ApiResponse.success(productService.getArAvailableProducts(pageable));

    }

}
