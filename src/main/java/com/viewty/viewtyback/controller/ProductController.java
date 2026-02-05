package com.viewty.viewtyback.controller;

import com.viewty.viewtyback.dto.response.ApiResponse;
import com.viewty.viewtyback.dto.response.ProductDetailResponse;
import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false) String name
    ) {
        return productService.findAll(name);
    }

    //  상품 상세 조회 (성분 분석 포함)
//    @GetMapping("/{id}")
//    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable Long id) {
//        ProductDetailResponse response = productService.getProductDetail(id);
//        return ApiResponse.success(response);
//    }

}
