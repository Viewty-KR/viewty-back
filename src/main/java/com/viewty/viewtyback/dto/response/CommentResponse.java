package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String name;
    private String content;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        String name = comment.getUser() == null ? null : comment.getUser().getName();
        return CommentResponse.builder()
                .id(comment.getId())
                .name(name)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
