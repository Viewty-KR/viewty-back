package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

@Getter
@Builder
public class ProductListResponse {
    private Long id;
    private String name;
    private long price;
    private String imgUrl;
    private String manufacturer;
    private String categoryName;

    public static ProductListResponse from(Product product) {
        String safeImgUrl = product.getImgUrl();
        try {
            if (safeImgUrl != null && !safeImgUrl.isBlank()) {
                int lastSlashIndex = safeImgUrl.lastIndexOf('/');
                if (lastSlashIndex != -1) {
                    String path = safeImgUrl.substring(0, lastSlashIndex + 1);
                    String filename = safeImgUrl.substring(lastSlashIndex + 1);

                    // 1. [1+1 보호]
                    String tempFilename = filename.replaceAll("(\\d)\\+(\\d)", "$1<PLUS>$2");

                    // 2. [디코딩]
                    try {
                        tempFilename = URLDecoder.decode(tempFilename, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        tempFilename = tempFilename.replace("+", " ");
                    }

                    // 3. [복구 & 수정]
                    tempFilename = tempFilename.replace("<PLUS>", "+");
                    tempFilename = tempFilename.replace("( ", "("); // 닥터자르트 오타 수정

                    // 4. [정규화 & 인코딩]
                    tempFilename = Normalizer.normalize(tempFilename, Normalizer.Form.NFC);
                    String encodedFilename = URLEncoder.encode(tempFilename, StandardCharsets.UTF_8);

                    // 5. [S3 호환]
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

        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .imgUrl(safeImgUrl)
                .manufacturer(product.getManufacturer())
                .categoryName(product.getCategoryId() != null ? product.getCategoryId().getName() : null)
                .build();
    }
}