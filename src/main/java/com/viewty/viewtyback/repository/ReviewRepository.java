package com.viewty.viewtyback.repository;

import com.viewty.viewtyback.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProduct_IdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 리뷰 ID와 사용자 ID로 리뷰 조회 (본인 확인용)
    Optional<Review> findByIdAndUserId(Long reviewId, Long userId);

}
