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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final RestrictedIngredientRepository restrictedIngredientRepository;

    /**
     * 상품 목록 조회
     */
    public Page<ProductListResponse> getProducts(String name, Pageable pageable) {
        Page<Product> products;

        if (name == null || name.trim().isEmpty()) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContaining(name, pageable);
        }

        return products.map(ProductListResponse::from);
    }

    /**
     * 상품 상세 조회 (성분 분석 및 상세 정보 포함)
     */
    public ProductDetailResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 1. [상품 정보 제공 고시용] 전성분 문자열 가져오기
        // Repository에서 GROUP_CONCAT으로 합쳐진 문자열을 가져옵니다.
        String allIngredients = productRepository.getAllProductIngredient(id);
        if (allIngredients == null) {
            allIngredients = ""; // 데이터가 없으면 빈 문자열 처리
        }

        // 2. [성분 분석용] 성분 리스트 가져오기 (ProductIngredientMap 이용)
        // Product 엔티티의 ingredientMaps를 통해 연결된 성분 객체들을 바로 가져옵니다.
        List<ProductIngredient> productIngredients = product.getIngredientMaps().stream()
                .map(ProductIngredientMap::getIngredient)
                .collect(Collectors.toList());

        // 3. 규제 성분 후보군 조회
        // 분석할 성분들의 이름과 영문명을 추출하여 후보군을 DB에서 조회합니다.
        List<String> ingredientNames = productIngredients.stream()
                .map(ProductIngredient::getName)
                .collect(Collectors.toList());

        List<String> engNames = productIngredients.stream()
                .map(ProductIngredient::getEngName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 성능 최적화를 위해 현재 상품에 포함된 성분명과 연관된 규제 성분만 DB에서 가져옵니다.
        List<RestrictedIngredient> restrictedCandidates =
                restrictedIngredientRepository.findByNamesOrEngNames(ingredientNames, engNames);

        // 4. 정밀 매칭 및 DTO 변환
        List<ProductDetailResponse.IngredientAnalysisDto> analyzedIngredients = productIngredients.stream()
                .map(prodIng -> {
                    // 스마트 매칭 로직을 통해 규제 성분 여부 판별
                    RestrictedIngredient matched = findMatchingRestrictedIngredient(prodIng, restrictedCandidates);
                    return ProductDetailResponse.IngredientAnalysisDto.of(prodIng, matched);
                })
                .collect(Collectors.toList());

        // 5. 최종 응답 생성 (전성분 문자열 포함)
        return ProductDetailResponse.of(product, analyzedIngredients, allIngredients);
    }

    /**
     * 🕵️‍♀️ 스마트 매칭 메서드
     * 상품 성분과 규제 성분 리스트를 비교하여 일치하는 규제 성분을 반환합니다.
     */
    private RestrictedIngredient findMatchingRestrictedIngredient(ProductIngredient prodIng, List<RestrictedIngredient> candidates) {
        for (RestrictedIngredient restricted : candidates) {

            // [1단계] CAS No 비교 (가장 정확함)
            if (isCasNoMatch(prodIng.getCasno(), restricted.getCasNo())) {
                return restricted;
            }

            // [2단계] 한글 이름 비교 (공백 제거 후 비교)
            if (isNameMatch(prodIng.getName(), restricted.getName())) {
                return restricted;
            }

            // [3단계] 영문 이름 비교 (대소문자 무시, 공백 제거)
            if (isNameMatch(prodIng.getEngName(), restricted.getEngName())) {
                return restricted;
            }
        }
        return null; // 매칭되는 규제 성분 없음 (안전)
    }

    /**
     * 🛠️ CAS 번호 비교 도우미
     * CAS 번호에 포함된 콤마(,), 슬래시(/), 특수 공백(NBSP) 등을 처리하여 비교합니다.
     */
    private boolean isCasNoMatch(String prodCas, String restrictedCas) {
        if (prodCas == null || restrictedCas == null) return false;

        // 규제 성분의 CAS 번호가 여러 개일 경우("10020-01-6, 73705-00-7") 분리하여 비교
        String[] restrictedCasList = restrictedCas.split("[,/]");

        for (String rCas : restrictedCasList) {
            // 공백 제거 및 NBSP(\u00A0) 제거
            String cleanRCas = rCas.trim().replaceAll("\\u00A0", "");
            String cleanPCas = prodCas.trim().replaceAll("\\u00A0", "");

            if (!cleanRCas.isEmpty() && cleanRCas.equals(cleanPCas)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 🛠️ 이름 비교 도우미
     * 띄어쓰기와 대소문자를 무시하고 이름을 비교합니다.
     */
    private boolean isNameMatch(String name1, String name2) {
        if (name1 == null || name2 == null) return false;
        // 모든 공백 제거 후 소문자로 변환하여 비교 ("소듐 벤조에이트" == "소듐벤조에이트")
        String clean1 = name1.replace(" ", "").toLowerCase();
        String clean2 = name2.replace(" ", "").toLowerCase();
        return clean1.equals(clean2);
    }
}