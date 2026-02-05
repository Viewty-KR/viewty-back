package com.viewty.viewtyback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {

    @NotBlank(message = "댓글을 작성해주세요.")
    @Size(max= 100, message = "댓글은 최대 100자까지 작성 가능합니다.")
    private String content;
}
