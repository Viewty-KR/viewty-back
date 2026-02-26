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

//    Page<Product> findByNameContaining(String name, Pageable pageable);

//    @EntityGraph(attributePaths = {"categoryId"})
//    @Query("SELECT p FROM Product p " +
//           "WHERE (:name IS NULL OR p.name LIKE %:name%) " +
//           "AND (:categoryId IS NULL OR p.categoryId.id = :categoryId) " +
//           "AND NOT EXISTS (" +
//           "  SELECT 1 FROM Product p2 " +
//           "  WHERE p2.name = p.name " +
//           "  AND (:name IS NULL OR p2.name LIKE %:name%) " +
//           "  AND (:categoryId IS NULL OR p2.categoryId.id = :categoryId) " +
//           "  AND p2.id < p.id" +
//           ")")
//    Page<Product> findUniqueProducts(@Param("name") String name, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT * FROM products
            WHERE id IN (
                SELECT MAX(id)
                FROM products
                GROUP BY category_id, price, img_url, capacity, country, cs_number, delivery_fee, delivery_jeju_fee, expiry_date, is_functional, manufacturer, name, prod_ingredients, qa, specifications, usage_method, precautions
            )
            AND category_id = :categoryId
            ORDER BY id DESC
            """,
            countQuery = """
                            SELECT DISTINCT count(*) FROM products
                            WHERE id IN(
                            SELECT MAX(id)
                            FROM products
                            GROUP BY category_id, price, img_url, capacity, country,
                            cs_number, delivery_fee, delivery_jeju_fee, expiry_date, is_functional,
                            manufacturer, name, prod_ingredients, qa, specifications, usage_method, precautions)
                            AND category_id =:categoryId
                            ORDER BY id DESC
            """,
            nativeQuery = true)
    Page<Product> findSpecCateProducts(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT * FROM products
            WHERE id IN (
                SELECT MAX(id)
                FROM products
                GROUP BY category_id, price, img_url, capacity, country, cs_number, delivery_fee, delivery_jeju_fee, expiry_date, is_functional, manufacturer, name, prod_ingredients, qa, specifications, usage_method, precautions
            )
            ORDER BY id DESC
            """,
            countQuery = """
                        SELECT COUNT(*) FROM products
                        WHERE id IN (
                            SELECT MAX(id)
                            FROM products
                            GROUP BY category_id, price, img_url, capacity, country, cs_number, delivery_fee, delivery_jeju_fee, expiry_date, is_functional, manufacturer, name, prod_ingredients, qa, specifications, usage_method, precautions
                        )
                        """,
            nativeQuery = true)
    Page<Product> findAllProducts(Pageable pageable);

    List<Product> findByName(String name); // [추가] 동일 이름의 모든 상품(옵션) 조회

    @NativeQuery("""
            SELECT GROUP_CONCAT(pi.name ORDER BY pi.name SEPARATOR', ') AS ingredient_names
            FROM product_ingredient pi
            JOIN product_ingredient_map pim ON pi.id = pim.ingredient_id
            WHERE pim.product_id = :productId
            GROUP BY pim.product_id;
            """)
    String getAllProductIngredient(@Param("productId") long productId);

    @Query(value = """
            SELECT p.* FROM products p
            WHERE p.id IN (
                SELECT MAX(p2.id)
                FROM products p2
                JOIN (
                    SELECT product_id FROM (
                        SELECT pim.product_id, pi.functional, COUNT(*) as func_count,
                               ROW_NUMBER() OVER (PARTITION BY pim.product_id ORDER BY COUNT(*) DESC, pi.functional ASC) as rn
                        FROM product_ingredient_map pim
                        JOIN product_ingredient pi ON pim.ingredient_id = pi.id
                        WHERE pi.functional IS NOT NULL
                        GROUP BY pim.product_id, pi.functional
                    ) t WHERE rn = 1 AND functional = :functionalType
                ) dp ON p2.id = dp.product_id
                GROUP BY p2.name  -- 💡 17개 컬럼 대신 '이름(name)'으로만 중복 제거!
            )
            ORDER BY p.id DESC
            """,
            countQuery = """
                        SELECT COUNT(DISTINCT p2.name)
                        FROM products p2
                        JOIN (
                            SELECT product_id FROM (
                                SELECT pim.product_id, pi.functional,
                                       ROW_NUMBER() OVER (PARTITION BY pim.product_id ORDER BY COUNT(*) DESC, pi.functional ASC) as rn
                                FROM product_ingredient_map pim
                                JOIN product_ingredient pi ON pim.ingredient_id = pi.id
                                WHERE pi.functional IS NOT NULL
                                GROUP BY pim.product_id, pi.functional
                            ) t WHERE rn = 1 AND functional = :functionalType
                        ) dp ON p2.id = dp.product_id
                        """,
            nativeQuery = true)
    Page<Product> findByFunctionalType(@Param("functionalType") String functionalType, Pageable pageable);

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

    /**
     * specifications 필드에서 키워드를 포함하는 제품 조회 (중복 제거)
     * 사용자 설문 정보 기반 제품 추천에 사용
     */
    @Query(value = """
            SELECT p.* FROM (
                SELECT DISTINCT *,
                    CASE
                        WHEN specifications LIKE CONCAT('%', :keyword, '%') THEN 1
                        WHEN name LIKE CONCAT('%', :keyword, '%') THEN 2
                        ELSE 3
                    END as priority
                FROM products
                WHERE id IN (
                    SELECT MAX(id)
                    FROM products
                    GROUP BY category_id, price, img_url, capacity, country, cs_number, delivery_fee, delivery_jeju_fee, expiry_date, is_functional, manufacturer, name, prod_ingredients, qa, specifications, usage_method, precautions
                )
                AND (specifications LIKE CONCAT('%', :keyword, '%')
                     OR name LIKE CONCAT('%', :keyword, '%')
                     OR prod_ingredients LIKE CONCAT('%', :keyword, '%'))
            ) p
            ORDER BY p.priority, p.id DESC
            """,
            countQuery = """
                        SELECT COUNT(*) FROM products
                        WHERE id IN (
                            SELECT MAX(id)
                            FROM products
                            GROUP BY category_id, price, img_url, capacity, country, cs_number, delivery_fee, delivery_jeju_fee, expiry_date, is_functional, manufacturer, name, prod_ingredients, qa, specifications, usage_method, precautions
                        )
                        AND (specifications LIKE CONCAT('%', :keyword, '%')
                             OR name LIKE CONCAT('%', :keyword, '%')
                             OR prod_ingredients LIKE CONCAT('%', :keyword, '%'))
                        """,
            nativeQuery = true)
    Page<Product> findBySpecificationsContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 다중 키워드 기반 제품 검색 (OR 조건)
     * 키워드는 "|"로 구분됨 (예: "보습|수분|영양")
     * specifications, name, prod_ingredients 필드에서 검색
     */
    @Query(value = """
            SELECT p.* FROM (
                SELECT DISTINCT *,
                    CASE
                        WHEN specifications REGEXP :keywords THEN 1
                        WHEN name REGEXP :keywords THEN 2
                        WHEN prod_ingredients REGEXP :keywords THEN 3
                        ELSE 4
                    END as priority
                FROM products
                WHERE id IN (
                    SELECT MAX(id)
                    FROM products
                    GROUP BY category_id, price, img_url, capacity, country, cs_number, delivery_fee, delivery_jeju_fee, expiry_date, is_functional, manufacturer, name, prod_ingredients, qa, specifications, usage_method, precautions
                )
                AND (specifications REGEXP :keywords
                     OR name REGEXP :keywords
                     OR prod_ingredients REGEXP :keywords)
            ) p
            ORDER BY p.priority, p.id DESC
            """,
            countQuery = """
                        SELECT COUNT(*) FROM products
                        WHERE id IN (
                            SELECT MAX(id)
                            FROM products
                            GROUP BY category_id, price, img_url, capacity, country, cs_number, delivery_fee, delivery_jeju_fee, expiry_date, is_functional, manufacturer, name, prod_ingredients, qa, specifications, usage_method, precautions
                        )
                        AND (specifications REGEXP :keywords
                             OR name REGEXP :keywords
                             OR prod_ingredients REGEXP :keywords)
                        """,
            nativeQuery = true)
    Page<Product> findByMultipleKeywords(@Param("keywords") String keywords, Pageable pageable);
}
