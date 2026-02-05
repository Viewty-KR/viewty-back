package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.request.CommentCreateRequest;
import com.viewty.viewtyback.dto.request.CommentUpdateRequest;
import com.viewty.viewtyback.dto.response.CommentResponse;
import com.viewty.viewtyback.entity.Comment;
import com.viewty.viewtyback.entity.Review;
import com.viewty.viewtyback.entity.User;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.CommentRepository;
import com.viewty.viewtyback.repository.ReviewRepository;
import com.viewty.viewtyback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public Page<CommentResponse> getCommentsByReview(Long reviewId, Pageable pageable) {
        validateReviewExists(reviewId);
        return commentRepository.findByReviewIdOrderByCreatedAtDesc(reviewId, pageable)
                .map(CommentResponse::from);
    }

    public Page<CommentResponse> getMyComments(Long userId, Pageable pageable) {
        validateUserExists(userId);
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(CommentResponse::from);
    }

    @Transactional
    public CommentResponse createComment(Long reviewId, Long userId, CommentCreateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .review(review)
                .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse updateMyComment(Long commentId, Long userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        validateOwnership(comment, userId);
        comment.update(request.getContent());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteMyComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        validateOwnership(comment, userId);
        commentRepository.delete(comment);
    }

    private void validateReviewExists(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new CustomException(ErrorCode.REVIEW_NOT_FOUND);
        }
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateOwnership(Comment comment, Long userId) {
        Long ownerId = comment.getUser() == null ? null : comment.getUser().getId();
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
        }
    }
}
