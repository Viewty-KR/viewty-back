package com.viewty.viewtyback.repository;

import com.viewty.viewtyback.entity.RestrictedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestrictedIngredientRepository extends JpaRepository<RestrictedIngredient, Long> {

    // 성분 이름 리스트에 포함된 금지 성분을 모두 조회
    @Query("SELECT r FROM RestrictedIngredient r WHERE r.name IN :names OR r.engName IN :engNames")
    List<RestrictedIngredient> findByNamesOrEngNames(@Param("names") List<String> names, @Param("engNames") List<String> engNames);
}
