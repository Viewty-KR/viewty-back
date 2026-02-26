package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.response.ProductDetailResponse;
import com.viewty.viewtyback.dto.response.ProductListResponse;
import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductIngredient;
import com.viewty.viewtyback.entity.RestrictedIngredient;
import com.viewty.viewtyback.entity.User;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.ProductRepository;
import com.viewty.viewtyback.repository.RestrictedIngredientRepository;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final RestrictedIngredientRepository restrictedIngredientRepository;
    private final UserRepository userRepository;

    /**
     * 상품 목록 조회 (중복 제거 및 필터링)
     */
//    public Page<ProductListResponse> getProducts(String name, Long categoryId, Pageable pageable) {
//        // [최적화] 서브쿼리를 통한 중복 제거 조회
//        Page<Product> products = productRepository.findUniqueProducts(name, categoryId, pageable);
//        return products.map(ProductListResponse::from);
//    }
    public Page<ProductListResponse> getProducts(Long categoryId, Pageable pageable) {
        // [최적화] 서브쿼리를 통한 중복 제거 조회
        Page<Product> products;
        if(categoryId == null){
            products = productRepository.findAllProducts(pageable);
        }else {
            products = productRepository.findSpecCateProducts(categoryId, pageable);
        }
        return products.map(product -> ProductListResponse.from(product));
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
                .map(optProduct -> {
                    // optProduct는 Product 엔티티입니다.
                    // Product에 연결된 ProductOption(옵션 테이블)에서 colorCode를 가져옵니다.
                    String color = null;
                    if (optProduct.getOptionId() != null) {
                        color = optProduct.getOptionId().getColorCode(); // 옵션 테이블에서 컬러 꺼내기
                    }

                    // null이 아니고 빈 칸이 아닐 때만 AR 지원(true)
                    boolean isAr = (color != null && !color.trim().isEmpty());

                    // [수정] product_options 테이블의 name을 우선 사용하고, 없으면 capacity 사용
                    String displayName = "";
                    if (optProduct.getOptionId() != null && optProduct.getOptionId().getName() != null) {
                    displayName = optProduct.getOptionId().getName();
                    }
                    return ProductDetailResponse.ProductOptionDto.builder()
                            .id(optProduct.getId())
                            .optionName(displayName)
                            .price(optProduct.getPrice())
                            .colorCode(color)
                            .isArAvailable(isAr)
                            .build();
                })
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

    // 사용자 설문 정보 기반 제품 추천
    public Page<ProductListResponse> getRecommendedProducts(Long userId, String keyword, Pageable pageable) {
        // 1. 키워드가 있으면 키워드로만 검색
        if (keyword != null && !keyword.trim().isEmpty()) {
            Page<Product> products = productRepository.findByMultipleKeywords(keyword.trim(), pageable);

             return products.map(ProductListResponse::from);
        }

        // 2. userId가 없으면 전체 제품 반환
        if (userId == null) {
            return getProducts(null, pageable);
        }

        // 3. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 4. 설문을 완료하지 않은 경우 전체 제품 반환
        if (user.getSkinType() == null || user.getSkinType().isEmpty()) {
            return getProducts(null, pageable);
        }

        // 5. 사용자 설문 정보를 기반으로 추천 키워드 리스트 생성
        List<String> keywords = buildRecommendationKeywords(user);

        // 6. 키워드 리스트를 하나의 검색어로 결합 (OR 조건)
        String combinedKeyword = String.join("|", keywords);

        // 7. 다중 키워드 기반 제품 조회
        Page<Product> products = productRepository.findByMultipleKeywords(combinedKeyword, pageable);

        // 8. 결과가 없으면 모든 피부 타입 키워드로 재검색
        if (products.isEmpty()) {
            String allSkinTypeKeywords = "모든 타입";
            products = productRepository.findByMultipleKeywords(allSkinTypeKeywords, pageable);
        }

        return products.map(ProductListResponse::from);
    }

    /**
     * 사용자 설문 정보를 기반으로 추천 키워드 리스트 생성
     * 피부 타입, 고민, 민감도를 모두 고려하여 다양한 키워드를 생성합니다.
     */
    private List<String> buildRecommendationKeywords(User user) {
        List<String> keywords = new java.util.ArrayList<>();

        // 1. 피부 고민 기반 키워드
        String concerns = user.getConcerns();
        if (concerns != null && !concerns.isEmpty()) {
            keywords.addAll(getConcernKeywords(concerns));
        }

        // 2. 피부 타입 기반 키워드
        String skinType = user.getSkinType();
        if (skinType != null && !skinType.isEmpty()) {
            keywords.addAll(getSkinTypeKeywords(skinType));
        }

        // 3. 민감도 기반 키워드
        String sensitivity = user.getSensitivity();
        if (sensitivity != null && !sensitivity.isEmpty()) {
            keywords.addAll(getSensitivityKeywords(sensitivity));
        }

        return keywords;
    }

    /**
     * 피부 타입별 키워드 (여러 개 반환 가능)
     * A: 건성 피부
     * B: 복합성 피부
     * C: 지성 피부
     */
    private List<String> getSkinTypeKeywords(String skinType) {
        return switch(skinType) {
            case "A" -> List.of("건성", "보습", "촉촉");
            case "B" -> List.of("복합성");
            case "C" -> List.of("지성", "피지", "모공", "산뜻");
            default -> List.of();
        };
    }

    /**
     * 피부 고민별 키워드
     * concerns는 "여드름,색소침착,주름" 형태로 저장됨
     */
    private List<String> getConcernKeywords(String concerns) {
        List<String> keywords = new java.util.ArrayList<>();
        String[] concernArray = concerns.split(",");

        for (String concern : concernArray) {
            concern = concern.trim();
            switch(concern) {
                case "좁쌀/화농성 여드름":
                    keywords.addAll(List.of("여드름", "트러블", "진정"));
                    break;
                case "기미/주근깨/잡티 (미백)":
                    keywords.addAll(List.of("톤업"));
                    break;
                case "주름/탄력 처짐":
                    keywords.addAll(List.of("주름", "탄력"));
                    break;
                case "블랙헤드/모공":
                    keywords.addAll(List.of("모공", "피지", "각질"));
                    break;
                case "홍조/피부 붉음":
                    keywords.addAll(List.of("민감", "진정", "순한"));
                    break;
            }
        }

        return keywords;
    }

    /**
     * 민감도별 키워드
     * A: 매우 민감, B: 보통, C: 민감하지 않음
     */
    private List<String> getSensitivityKeywords(String sensitivity) {
        return switch(sensitivity) {
            case "A" -> List.of("순한", "민감", "진정", "무향");
            case "B" -> List.of("순한");
            default -> List.of();
        };
    }
}
