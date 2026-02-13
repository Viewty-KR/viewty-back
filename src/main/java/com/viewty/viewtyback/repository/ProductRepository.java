package com.viewty.viewtyback.repository;

import com.viewty.viewtyback.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContaining(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"categoryId"})
    @Query("SELECT p FROM Product p " +
           "WHERE (:name IS NULL OR p.name LIKE %:name%) " +
           "AND (:categoryId IS NULL OR p.categoryId.id = :categoryId) " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM Product p2 " +
           "  WHERE p2.name = p.name " +
           "  AND (:name IS NULL OR p2.name LIKE %:name%) " +
           "  AND (:categoryId IS NULL OR p2.categoryId.id = :categoryId) " +
           "  AND p2.id < p.id" +
           ")")
    Page<Product> findUniqueProducts(@Param("name") String name, @Param("categoryId") Long categoryId, Pageable pageable);

    List<Product> findByName(String name); // [추가] 동일 이름의 모든 상품(옵션) 조회

    @NativeQuery("""
            SELECT GROUP_CONCAT(pi.name ORDER BY pi.name SEPARATOR', ') AS ingredient_names
            FROM product_ingredient pi
            JOIN product_ingredient_map pim ON pi.id = pim.ingredient_id
            WHERE pim.product_id = :productId
            GROUP BY pim.product_id;
            """)
    String getAllProductIngredient(@Param("productId") long productId);

    /**
     * 상품 ID를 기반으로 전성분, 주의 성분 정보, 그리고 효능(Effectiveness)을 한 번에 조회합니다.
     * PEG/설페이트와 같은 특수 주의 성분 판별도 쿼리 레벨에서 처리합니다.
     */
    @Query("SELECT pi, ri, " +
           "CASE " +
           "  WHEN (pi.name LIKE '%피이지%' OR pi.name LIKE '%PEG%' OR pi.engName LIKE '%PEG%' OR pi.engName LIKE '%POLYETHYLENE GLYCOL%') THEN '주의성분(PEG)' " +
           "  WHEN (pi.name LIKE '%라우릴설페이트%' OR pi.name LIKE '%라우레스설페이트%' OR pi.engName LIKE '%LAURYL SULFATE%' OR pi.engName LIKE '%LAURETH SULFATE%') THEN '주의성분(설페이트)' " +
           "  ELSE pi.functional " +
           "END FROM ProductIngredient pi " +
           "JOIN ProductIngredientMap pim ON pi.id = pim.ingredient.id " +
           "LEFT JOIN RestrictedIngredient ri ON " +
           "(pi.casno IS NOT NULL AND ri.casNo IS NOT NULL AND pi.casno = ri.casNo) OR " +
           "(pi.name = ri.name) OR " +
           "(pi.engName = ri.engName) " +
           "WHERE pim.product.id = :productId")
    List<Object[]> findIngredientsWithAnalysis(@Param("productId") Long productId);
}
