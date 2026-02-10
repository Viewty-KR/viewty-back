package com.viewty.viewtyback.repository;

import com.viewty.viewtyback.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContaining(String name, Pageable pageable);

    @NativeQuery("""
            SELECT GROUP_CONCAT(pi.name ORDER BY pi.name SEPARATOR', ') AS ingredient_names
            FROM product_ingredient pi
            JOIN product_ingredient_map pim ON pi.id = pim.ingredient_id
            WHERE pim.product_id = :productId
            GROUP BY pim.product_id;
            """)
    String getAllProductIngredient(@Param("productId") long productId);
}
