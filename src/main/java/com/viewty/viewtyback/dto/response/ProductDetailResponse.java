package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductIngredient;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String price;
    private String manufacturer; // 제조사 (신뢰도 확인용 등으로 유지)

    // 분석된 성분 정보
    private List<IngredientAnalysisDto> ingredients;
    private int harmfulIngredientCount; // 유해 성분 개수 요약

    public static ProductDetailResponse of(Product product, List<IngredientAnalysisDto> analyzedIngredients) {
        // 유해 성분(주의 성분) 개수 카운팅
        int harmfulCount = (int) analyzedIngredients.stream()
                .filter(IngredientAnalysisDto::isHarmful)
                .count();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())  //이름
                .price(product.getPrice()) //가격
                .manufacturer(product.getManufacturer()) //제조사
                .ingredients(analyzedIngredients)  // 성분 정보
                .harmfulIngredientCount(harmfulCount) // 유해 성분 갯수
                .build();
    }

    @Getter
    @Builder
    public static class IngredientAnalysisDto {
        private String name;
        private String ewgGrade; // EWG 등급 (예: "1-2")
        private boolean isHarmful; // 주의 성분 여부
        private boolean isAllergyTrigger; // 알레르기 유발 여부

        // 성분 엔티티 -> DTO 변환
        public static IngredientAnalysisDto from(ProductIngredient ingredient) {
            return IngredientAnalysisDto.builder()
                    .name(ingredient.getName())
                    // 예시 데이터 매핑
                    // .ewgGrade(ingredient.getEwgGrade())
                    // .isHarmful(ingredient.getRiskLevel() > 3)
                    .build();
        }
    }
}