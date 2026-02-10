package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.response.ProductDetailResponse;
import com.viewty.viewtyback.dto.response.ProductListResponse;
import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductIngredient;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.IngredientRepository;
import com.viewty.viewtyback.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;


    public Page<ProductListResponse> getProducts(String name, Pageable pageable){
        Page<Product> products;

        if (name == null || name.trim().isEmpty()) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContaining(name, pageable);
        }

        // Entity(Product)를 DTO(ProductListResponse)로 변환
        return products.map(ProductListResponse::from);
    }

    public ProductDetailResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        String rawIngredients = product.getProdIngredients();
        if (rawIngredients == null || rawIngredients.trim().isEmpty()) {
            return ProductDetailResponse.of(product, List.of());
        }

        List<String> ingredientNames = Arrays.stream(rawIngredients.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());

        List<ProductIngredient> ingredients = ingredientRepository.findByNameIn(ingredientNames);
        List<ProductDetailResponse.IngredientAnalysisDto> analyzedIngredients = ingredients.stream()
                .map(ProductDetailResponse.IngredientAnalysisDto::from)
                .collect(Collectors.toList());

        return ProductDetailResponse.of(product, analyzedIngredients);
    }
}
