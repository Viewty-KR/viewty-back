package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.response.ProductDetailResponse;
import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductIngredient;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.IngredientRepository;
import com.viewty.viewtyback.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public List<Product> findAll(String name){
        if (name == null || name.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.findByNameContaining(name);
    }

//    public ProductDetailResponse getProductDetail(Long id) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
//
//        String rawIngredients = product.getProdIngredients();
//        if (rawIngredients == null || rawIngredients.trim().isEmpty()) {
//            return ProductDetailResponse.of(product, List.of());
//        }
//
//        List<String> ingredientNames = Arrays.stream(rawIngredients.split(","))
//                .map(String::trim)
//                .filter(name -> !name.isEmpty())
//                .collect(Collectors.toList());
//
//        List<ProductIngredient> ingredients = ingredientRepository.findByNameIn(ingredientNames);
//        List<ProductDetailResponse.IngredientAnalysisDto> analyzedIngredients = ingredients.stream()
//                .map(ProductDetailResponse.IngredientAnalysisDto::from)
//                .collect(Collectors.toList());
//
//        return ProductDetailResponse.of(product, analyzedIngredients);
//    }
}
