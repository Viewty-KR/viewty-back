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
    @Query("SELECT p FROM Product p WHERE p.id IN (" +
           "  SELECT MIN(p2.id) FROM Product p2 " +
           "  WHERE (:name IS NULL OR p2.name LIKE %:name%) " +
           "  AND (:categoryId IS NULL OR p2.categoryId.id = :categoryId) " +
           "  GROUP BY p2.name" +
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
}
