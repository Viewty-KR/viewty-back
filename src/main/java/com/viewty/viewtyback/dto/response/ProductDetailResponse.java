package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.Product;
import com.viewty.viewtyback.entity.ProductIngredient;
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

    public static ProductDetailResponse of(Product product, List<IngredientAnalysisDto> analyzedIngredients) {
        int harmfulCount = (int) analyzedIngredients.stream()
                .filter(IngredientAnalysisDto::isHarmful)
                .count();

        String safeImgUrl = product.getImgUrl();
        try {
            if (safeImgUrl != null && !safeImgUrl.isBlank()) {
                int lastSlashIndex = safeImgUrl.lastIndexOf('/');
                if (lastSlashIndex != -1) {
                    String path = safeImgUrl.substring(0, lastSlashIndex + 1);
                    String filename = safeImgUrl.substring(lastSlashIndex + 1);

                    // [1단계] "1+1+1" 보호 작전!
                    // 숫자와 숫자 사이의 '+'는 '잠시 다른 문자(<PLUS>)'로 바꿔둡니다.
                    // 그래야 밑에서 디코딩할 때 공백으로 사라지지 않습니다.
                    String tempFilename = filename.replaceAll("(\\d)\\+(\\d)", "$1<PLUS>$2");

                    // [2단계] 디코딩 (나머지 '+'는 공백으로 변환됨)
                    try {
                        tempFilename = URLDecoder.decode(tempFilename, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        // 10% 등 에러 발생 시, 그냥 '+'를 공백으로 바꾸고 진행
                        tempFilename = tempFilename.replace("+", " ");
                    }

                    // [3단계] 보호해뒀던 '+' 복구 (1<PLUS>1 -> 1+1)
                    // 이제 "1+1"은 살아났고, "닥터자르트+토너"는 "닥터자르트 토너"가 되었습니다.
                    tempFilename = tempFilename.replace("<PLUS>", "+");

                    // [4단계] 닥터자르트 괄호 오타 수정 ("( 면봉" -> "(면봉")
                    tempFilename = tempFilename.replace("( ", "(");
                    tempFilename = tempFilename.replace(" )", ")");

                    // [5단계] 정규화 (한글 자모 합치기)
                    tempFilename = Normalizer.normalize(tempFilename, Normalizer.Form.NFC);

                    // [6단계] 최종 인코딩 (S3가 알아듣는 주소로 변환)
                    String encodedFilename = URLEncoder.encode(tempFilename, StandardCharsets.UTF_8);

                    // [7단계] 마무리 치환 (공백 -> %20, 괄호 -> %28 %29)
                    // 여기서 '+'는 원래 공백이었던 애들이 변한 것이므로 %20으로 바꿉니다.
                    // 진짜 '+'는 위에서 '%2B'로 인코딩되었으므로 안전합니다.
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
                .build();
    }

    @Getter
    @Builder
    public static class IngredientAnalysisDto {
        private String name;
        private String ewgGrade;
        private boolean isHarmful;
        private boolean isAllergyTrigger;

        public static IngredientAnalysisDto from(ProductIngredient ingredient) {
            return IngredientAnalysisDto.builder()
                    .name(ingredient.getName())
                    .build();
        }
    }
}