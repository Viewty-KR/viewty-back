package com.viewty.viewtyback.repository;

import com.viewty.viewtyback.entity.ProductBookMark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductBookMarkRepository extends JpaRepository<ProductBookMark, Long> {

    // bookmark 존재 여부 확인
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // bookmark 해제
    void deleteByUserIdAndProductId(Long userId, Long productId);

    // 내가 bookmark 한 상품 조회
    List<ProductBookMark> findByUserIdOrderByCreatedAtDesc(Long userId);
}
