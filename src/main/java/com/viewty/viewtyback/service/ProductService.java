package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.response.ProductDetailResponse;
import com.viewty.viewtyback.dto.response.ProductListResponse;
import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductIngredient;
import com.viewty.viewtyback.entity.ProductIngredientMap;
import com.viewty.viewtyback.entity.RestrictedIngredient;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.ProductRepository;
import com.viewty.viewtyback.repository.RestrictedIngredientRepository;
import jakarta.annotation.PostConstruct;
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

    // [캐싱] 7,000여 개의 제한 성분 데이터를 메모리에 캐싱하여 DB 쿼리 폭주 방지
    private List<RestrictedIngredient> cachedRestrictedIngredients;

    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * 캐시 데이터 갱신
     */
    public synchronized void refreshCache() {
        this.cachedRestrictedIngredients = restrictedIngredientRepository.findAll();
    }

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
        // 1. 상품 조회 (성분 맵까지 한 번에 가져오기 위해 조인 활용 권장 - 여기서는 단건 조회)
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 2. 전성분 문자열 가져오기 (성능을 위해 Repository 단독 쿼리 유지)
        String allIngredients = productRepository.getAllProductIngredient(id);
        if (allIngredients == null) {
            allIngredients = "";
        }

        // 3. 성분 리스트 추출
        List<ProductIngredient> productIngredients = product.getIngredientMaps().stream()
                .map(ProductIngredientMap::getIngredient)
                .collect(Collectors.toList());

        // 4. [최적화] 캐시된 제한 성분 데이터 사용 (매번 DB 호출 안 함)
        if (cachedRestrictedIngredients == null) refreshCache();
        List<RestrictedIngredient> restrictedCandidates = cachedRestrictedIngredients;

        // 5. 정밀 매칭 및 DTO 변환
        List<ProductDetailResponse.IngredientAnalysisDto> analyzedIngredients = productIngredients.stream()
                .map(prodIng -> {
                    RestrictedIngredient matched = findMatchingRestrictedIngredient(prodIng, restrictedCandidates);
                    String effectiveness = getIngredientEffectiveness(prodIng);
                    return ProductDetailResponse.IngredientAnalysisDto.of(prodIng, matched, effectiveness);
                })
                .collect(Collectors.toList());

        // 6. 옵션 목록 조회 (이름이 같은 상품들)
        List<ProductDetailResponse.ProductOptionDto> options = productRepository.findByName(product.getName()).stream()
                .map(opt -> ProductDetailResponse.ProductOptionDto.builder()
                        .id(opt.getId())
                        .optionName(opt.getCapacity() != null ? opt.getCapacity() : "옵션 " + opt.getId())
                        .price(opt.getPrice())
                        .build())
                .collect(Collectors.toList());

        // 7. 최종 응답 생성
        return ProductDetailResponse.of(product, analyzedIngredients, allIngredients, options);
    }

    /**
     * 🕵️‍♀️ 스마트 매칭 메서드 (메모리 내의 candidates 활용)
     */
    private RestrictedIngredient findMatchingRestrictedIngredient(ProductIngredient prodIng, List<RestrictedIngredient> candidates) {
        
        // [1단계] 특수 주의 성분(PEG, 설페이트) 자동 감지
        if (isSpecialCautionIngredient(prodIng)) {
            return createVirtualRestrictedIngredient("20가지 주의 성분", "주의 성분 계열(PEG/설페이트)");
        }

        for (RestrictedIngredient restricted : candidates) {
            // [2단계] CAS No 비교
            if (isCasNoMatch(prodIng.getCasno(), restricted.getCasNo())) {
                return restricted;
            }

            // [3단계] 한글 이름 비교
            if (isNameMatch(prodIng.getName(), restricted.getName())) {
                return restricted;
            }

            // [4단계] 영문 이름 비교
            if (isNameMatch(prodIng.getEngName(), restricted.getEngName())) {
                return restricted;
            }
        }
        return null;
    }

    private boolean isSpecialCautionIngredient(ProductIngredient prodIng) {
        String name = (prodIng.getName() != null) ? prodIng.getName().toUpperCase() : "";
        String engName = (prodIng.getEngName() != null) ? prodIng.getEngName().toUpperCase() : "";

        boolean isPeg = name.contains("피이지") || name.contains("PEG") || 
                        engName.contains("PEG") || engName.contains("POLYETHYLENE GLYCOL");

        boolean isSulfate = name.contains("라우릴설페이트") || name.contains("라우레스설페이트") || 
                            engName.contains("LAURYL SULFATE") || engName.contains("LAURETH SULFATE");
        
        return isPeg || isSulfate;
    }

    private String getIngredientEffectiveness(ProductIngredient ing) {
        String name = (ing.getName() != null) ? ing.getName() : "";
        if (name.contains("글리세린") || name.contains("부틸렌글라이콜") || name.contains("하이알루로네이트") || name.contains("꿀추출물") || name.contains("솔비톨")) return "피부 보습";
        if (name.contains("세라마이드") || name.contains("콜레스테롤") || name.contains("스쿠알란") || name.contains("필라그린") || name.contains("레시틴")) return "피부 보호";
        if (name.contains("병풀") || name.contains("알란토인") || name.contains("판테놀") || name.contains("마데카소사이드") || name.contains("캐모마일") || name.contains("쑥") || name.contains("칼라민")) return "수렴 진정";
        if (name.contains("나이아신아마이드") || name.contains("비타민C") || name.contains("아스코빅") || name.contains("글루타티온")) return "피부 미백";
        if (name.contains("아데노신") || name.contains("펩타이드") || name.contains("레티놀") || name.contains("바쿠치올")) return "주름 개선";
        return null;
    }

    private RestrictedIngredient createVirtualRestrictedIngredient(String division, String name) {
        return RestrictedIngredient.builder()
                .division(division)
                .name(name)
                .build();
    }

    private boolean isCasNoMatch(String prodCas, String restrictedCas) {
        if (prodCas == null || restrictedCas == null) return false;
        String[] restrictedCasList = restrictedCas.split("[,/]");
        for (String rCas : restrictedCasList) {
            String cleanRCas = rCas.trim().replaceAll("\\u00A0", "");
            String cleanPCas = prodCas.trim().replaceAll("\\u00A0", "");
            if (!cleanRCas.isEmpty() && cleanRCas.equals(cleanPCas)) return true;
        }
        return false;
    }

    private boolean isNameMatch(String name1, String name2) {
        if (name1 == null || name2 == null) return false;
        String clean1 = name1.replaceAll("[\\s,.]", "").toLowerCase();
        String clean2 = name2.replaceAll("[\\s,.]", "").toLowerCase();
        return clean1.equals(clean2) || (clean1.length() > 2 && clean2.length() > 2 && (clean1.contains(clean2) || clean2.contains(clean1)));
    }
}
