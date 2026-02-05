package com.viewty.viewtyback.repository;

import com.viewty.viewtyback.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<ProductIngredient, Long> {

    List<ProductIngredient> findByNameIn(List<String> names);
}
