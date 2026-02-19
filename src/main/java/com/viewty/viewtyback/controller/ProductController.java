package com.viewty.viewtyback.controller;

import com.viewty.viewtyback.dto.response.ApiResponse;
import com.viewty.viewtyback.dto.response.ProductDetailResponse;
import com.viewty.viewtyback.dto.response.ProductListResponse;
import com.viewty.viewtyback.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

//      상품 상세 조회 (성분 분석 포함)
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable Long id) {
        ProductDetailResponse response = productService.getProductDetail(id);
        return ApiResponse.success(response);
    }

}
