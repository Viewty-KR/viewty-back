package com.viewty.viewtyback.repository;

import com.viewty.viewtyback.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByReviewIdOrderByCreatedAtDesc(Long reviewId);

    void deleteByReviewId(Long reviewId);

    Page<Comment> findByReviewIdOrderByCreatedAtDesc(Long reviewId, Pageable pageable);

    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
