package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.response.ProductDetailResponse;
import com.viewty.viewtyback.dto.response.ProductListResponse;
import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductIngredient;
import com.viewty.viewtyback.entity.RestrictedIngredient;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.ProductRepository;
import com.viewty.viewtyback.repository.RestrictedIngredientRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final RestrictedIngredientRepository restrictedIngredientRepository;

    /**
     * 상품 목록 조회 (중복 제거 및 필터링)
     */
    public Page<ProductListResponse> getProducts(String name, Long categoryId, Pageable pageable) {
        // [최적화] 서브쿼리를 통한 중복 제거 조회
        Page<Product> products = productRepository.findUniqueProducts(name, categoryId, pageable);
        return products.map(ProductListResponse::from);
    }

    /**
     * 상품 상세 조회 (성분 분석 및 상세 정보 포함)
     */
    public ProductDetailResponse getProductDetail(Long id) {
        // 1. 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 2. 전성분 문자열 가져오기
        String allIngredients = productRepository.getAllProductIngredient(id);
        if (allIngredients == null) {
            allIngredients = "";
        }

        // 3. [최적화] 쿼리 조인을 통해 성분과 주의 성분 분석 정보를 한 번에 가져옴
        // 더 이상 자바 메모리 루프를 돌지 않고 DB 레벨에서 조인 처리
        List<Object[]> analysisResult = productRepository.findIngredientsWithAnalysis(id);

        // 4. 분석 결과 매핑 및 DTO 변환
        List<ProductDetailResponse.IngredientAnalysisDto> analyzedIngredients = analysisResult.stream()
                .map(row -> {
                    ProductIngredient prodIng = (ProductIngredient) row[0];
                    RestrictedIngredient matched = (RestrictedIngredient) row[1];
                    String effectiveness = (String) row[2]; // 쿼리에서 이미 계산된 효능 정보
                    
                    // DB에 없는 가상 주의 성분 처리 (PEG/설페이트 등 쿼리에서 식별된 경우)
                    if (matched == null && effectiveness != null && effectiveness.startsWith("주의성분")) {
                        matched = createVirtualRestrictedIngredient("20가지 주의 성분", effectiveness);
                    }
                    
                    return ProductDetailResponse.IngredientAnalysisDto.of(prodIng, matched, effectiveness);
                })
                .collect(Collectors.toList());

        // 5. 옵션 목록 조회 (이름이 같은 상품들)
        List<ProductDetailResponse.ProductOptionDto> options = productRepository.findByName(product.getName()).stream()
                .map(opt -> ProductDetailResponse.ProductOptionDto.builder()
                        .id(opt.getId())
                        .optionName(opt.getCapacity() != null ? opt.getCapacity() : "옵션 " + opt.getId())
                        .price(opt.getPrice())
                        .build())
                .collect(Collectors.toList());

        // 6. 최종 응답 생성
        return ProductDetailResponse.of(product, analyzedIngredients, allIngredients, options);
    }

    /**
     * DB 매칭되지 않은 특수 주의 성분을 위한 가상 객체 생성
     */
    private RestrictedIngredient createVirtualRestrictedIngredient(String division, String name) {
        return RestrictedIngredient.builder()
                .division(division)
                .name(name)
                .build();
    }
}
