package com.viewty.viewtyback.controller;

import com.viewty.viewtyback.dto.request.CommentCreateRequest;
import com.viewty.viewtyback.dto.request.CommentUpdateRequest;
import com.viewty.viewtyback.dto.response.ApiResponse;
import com.viewty.viewtyback.dto.response.CommentResponse;
import com.viewty.viewtyback.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiResponse<Page<CommentResponse>> getCommentsByReview(
            @RequestParam Long reviewId,
            Pageable pageable
    ) {
        return ApiResponse.success(commentService.getCommentsByReview(reviewId, pageable));
    }

    @GetMapping("/me")
    public ApiResponse<Page<CommentResponse>> getMyComments(
            @RequestParam Long userId,
            Pageable pageable
    ) {
        return ApiResponse.success(commentService.getMyComments(userId, pageable));
    }

    @PostMapping
    public ApiResponse<CommentResponse> createComment(
            @RequestParam Long reviewId,
            @RequestParam Long userId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        return ApiResponse.success(commentService.createComment(reviewId, userId, request));
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateMyComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestBody @Valid CommentUpdateRequest request
    ) {
        return ApiResponse.success(commentService.updateMyComment(commentId, userId, request));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteMyComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        commentService.deleteMyComment(commentId, userId);
        return ApiResponse.success(null);
    }
}
