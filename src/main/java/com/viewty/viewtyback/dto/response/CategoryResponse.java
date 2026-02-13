package com.viewty.viewtyback.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String cateCode;

    public static CategoryResponse from(com.viewty.viewtyback.entity.ProductCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .cateCode(category.getCateCode())
                .build();
    }
}
