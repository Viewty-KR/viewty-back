package com.viewty.viewtyback.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private String capacity;
    private String specifications;
    private String expiryDate;
    private String usageMethod;
    private String country;
    private String isFunctional;
    private String precautions;
    private String qa;
    private String csNumber;
    private String deliveryFee;
    private String deliveryJejuFee;
    private String allIngredients;
    private List<ProductOptionDto> options; // [추가] 상품 옵션 목록

    public static ProductDetailResponse of(Product product, List<IngredientAnalysisDto> analyzedIngredients, String allIngredients, List<ProductOptionDto> options) {
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
                .options(options)
                .build();
    }

    @Getter
    @Builder
    public static class ProductOptionDto {
        private Long id;
        private String optionName; // 옵션명
        private long price;
        private String colorCode;
        @JsonProperty("isArAvailable")
        private boolean isArAvailable;
    }

    @Getter
    @Builder
    public static class IngredientAnalysisDto {
        private String name;

        @JsonProperty("isHarmful")
        private boolean isHarmful;

        @JsonProperty("isCaution")
        private boolean isCaution;

        @JsonProperty("isAllergy")
        private boolean isAllergy;

        private String division;
        private String effectiveness;

        public static IngredientAnalysisDto of(ProductIngredient ingredient, RestrictedIngredient restrictedInfo, String effectiveness) {
            boolean isRestricted = (restrictedInfo != null);
            String division = isRestricted ? restrictedInfo.getDivision() : null;

            boolean isAllergy = division != null && division.contains("알레르기");
            boolean isCaution = division != null && (division.contains("20가지") || division.contains("주의"));
            boolean isLegalHarmful = division != null && (division.contains("금지") || division.contains("한도"));

            return IngredientAnalysisDto.builder()
                    .name(ingredient.getName())
                    .isHarmful(isLegalHarmful)
                    .isCaution(isCaution)
                    .isAllergy(isAllergy)
                    .division(division)
                    .effectiveness(effectiveness)
                    .build();
        }
    }
}
