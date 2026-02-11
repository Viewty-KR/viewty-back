package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductIngredient;
import com.viewty.viewtyback.entity.RestrictedIngredient;
import lombok.Builder;
import lombok.Getter;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;

@Getter
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private long price;
    private String manufacturer;
    private String imgUrl;
    private List<IngredientAnalysisDto> ingredients;
    private int harmfulIngredientCount;

    private String capacity;        // 용량
    private String specifications;  // 주요 사양
    private String expiryDate;      // 사용 기한
    private String usageMethod;     // 사용 방법
    private String country;         // 제조국
    private String isFunctional;    // 기능성 여부
    private String precautions;     // 주의사항
    private String qa;              // 품질보증기준
    private String csNumber;        // CS 전화번호
    private String deliveryFee;     // 배송비
    private String deliveryJejuFee; // 제주 도서산간 배송비
    private String allIngredients; // 전 성분

    public static ProductDetailResponse of(Product product, List<IngredientAnalysisDto> analyzedIngredients, String allIngredients) {
        // [수정] 유해 성분 개수 카운팅 (법적 규제 + 주의 성분 모두 포함하거나, 기획에 따라 isHarmful만 셀 수도 있음)
        // 여기서는 '주의가 필요한 모든 성분'을 카운트하도록 설정했습니다.
        int harmfulCount = (int) analyzedIngredients.stream()
                .filter(i -> i.isHarmful() || i.isCaution() || i.isAllergy())
                .count();

        String safeImgUrl = product.getImgUrl();
        try {
            if (safeImgUrl != null && !safeImgUrl.isBlank()) {
                int lastSlashIndex = safeImgUrl.lastIndexOf('/');
                if (lastSlashIndex != -1) {
                    String path = safeImgUrl.substring(0, lastSlashIndex + 1);
                    String filename = safeImgUrl.substring(lastSlashIndex + 1);
                    String tempFilename = filename.replaceAll("(\\d)\\+(\\d)", "$1<PLUS>$2");
                    try {
                        tempFilename = URLDecoder.decode(tempFilename, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        tempFilename = tempFilename.replace("+", " ");
                    }
                    tempFilename = tempFilename.replace("<PLUS>", "+");
                    tempFilename = tempFilename.replace("( ", "(");
                    tempFilename = tempFilename.replace(" )", ")");
                    tempFilename = Normalizer.normalize(tempFilename, Normalizer.Form.NFC);
                    String encodedFilename = URLEncoder.encode(tempFilename, StandardCharsets.UTF_8);
                    encodedFilename = encodedFilename.replace("+", "%20")
                            .replace("(", "%28")
                            .replace(")", "%29")
                            .replace("*", "%2A")
                            .replace("%7E", "~");
                    safeImgUrl = path + encodedFilename;
                }
            }
        } catch (Exception e) {
            System.out.println("이미지 변환 오류: " + e.getMessage());
        }

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .imgUrl(safeImgUrl)
                .manufacturer(product.getManufacturer())
                .ingredients(analyzedIngredients)
                .harmfulIngredientCount(harmfulCount)
                .capacity(product.getCapacity())
                .specifications(product.getSpecifications())
                .expiryDate(product.getExpiryDate())
                .usageMethod(product.getUsageMethod())
                .country(product.getCountry())
                .isFunctional(product.getIsFunctional())
                .precautions(product.getPrecautions())
                .qa(product.getQa())
                .csNumber(product.getCsNumber())
                .deliveryFee(product.getDeliveryFee())
                .deliveryJejuFee(product.getDeliveryJejuFee())
                .allIngredients(allIngredients)
                .build();
    }

    @Getter
    @Builder
    public static class IngredientAnalysisDto {
        private String name;
        private boolean isHarmful; // 법적 규제 (식약처 금지/한도)
        private boolean isCaution; // [추가] 20가지 주의 성분
        private boolean isAllergy; // [추가] 알레르기 유발 성분
        private String division;   // 사유 (예: "배합한도", "20가지 주의성분")

        public static IngredientAnalysisDto of(ProductIngredient ingredient, RestrictedIngredient restrictedInfo) {
            boolean isRestricted = (restrictedInfo != null);
            String division = isRestricted ? restrictedInfo.getDivision() : null;

            // [핵심 로직] division 텍스트를 분석하여 플래그 설정
            // DB의 division 컬럼에 "알레르기", "20가지", "주의" 등의 텍스트가 포함되어 있어야 함
            boolean isAllergy = division != null && division.contains("알레르기");
            boolean isCaution = division != null && (division.contains("20가지") || division.contains("주의"));

            // 법적 규제(Harmful)는 "금지"나 "한도"라는 단어가 있을 때만 true (알레르기/단순주의 제외)
            boolean isLegalHarmful = division != null && (division.contains("금지") || division.contains("한도"));

            return IngredientAnalysisDto.builder()
                    .name(ingredient.getName())
                    .isHarmful(isLegalHarmful) // 법적 규제만 Harmful로 취급
                    .isCaution(isCaution)      // 주의 성분 플래그
                    .isAllergy(isAllergy)      // 알레르기 플래그
                    .division(division)
                    .build();
        }
    }
}