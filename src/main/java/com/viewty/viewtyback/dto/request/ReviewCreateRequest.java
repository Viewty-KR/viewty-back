package com.viewty.viewtyback.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    @NotBlank
    private String name;

    @NotBlank(message = "리뷰를 작성해주세요.")
    @Size(min = 3, max = 500, message = "리뷰 내용은 10자 이상 500자 이하로 작성해주세요.")
    private String content;

    @Min(1)
    @Max(5)
    private int rating;
}
